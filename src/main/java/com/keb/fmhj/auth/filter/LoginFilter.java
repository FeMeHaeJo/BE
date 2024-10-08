package com.keb.fmhj.auth.filter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keb.fmhj.auth.domain.RefreshEntity;
import com.keb.fmhj.auth.repository.RefreshRepository;
import com.keb.fmhj.auth.utils.JWTUtil;
import com.keb.fmhj.global.exception.ErrorCode;
import com.keb.fmhj.global.exception.YouthException;
import com.keb.fmhj.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final RefreshRepository refreshRepository;
    private final JWTUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager,
                       RefreshRepository refreshRepository,
                       JWTUtil jwtUtil, String url) {

        this.authenticationManager = authenticationManager;
        this.refreshRepository = refreshRepository;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl(url);
    }

    // 로그인 시도시 작동
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
            AuthenticationException {
        // request 받은 json 값을 추출함
        Map<String, String> loginInfo = getLoginInfoFromJson(request);
        String loginId = loginInfo.get("loginId");
        String password = loginInfo.get("password");
        // 해당 값이 없을시 예외처리
        if (loginId == null) {
            throw YouthException.from(ErrorCode.INVALID_REQUEST);
        }
        if (password == null) {
            throw YouthException.from(ErrorCode.INVALID_REQUEST);
        }

        // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password);
        // token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        //인증에 성공한 loginId 받아오기
        String loginId = authResult.getName();

        //토큰 생성 - access 토큰 유효기간 30분
        String accessToken = jwtUtil.createJwt("access", loginId, 30 * 60 * 1000L);

        String refresh = jwtUtil.createJwt("refresh", "fakeLoginId", 24 * 60 * 60 * 1000L);
        response.addHeader("Authorization", "Bearer " + accessToken);// 헤더에 access 토큰 넣기
        response.addHeader("Set-Cookie", createCookie("refresh", refresh).toString()); //쿠키 생성밒 추가

        RefreshEntity refreshToken = new RefreshEntity(refresh, loginId);

        //API 응답 생성
        createAPIResponse(response, ErrorCode.REQUEST_OK);
    }

    // 로그인 실패시 작동
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        // API 응답 생성 - failed 예외는 BadCredentialsException 하나로만 처리함
        createAPIResponse(response, ErrorCode.INVALID_AUTH_TOKEN);
    }

    private ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .path("/") //쿠키 경로 설정(=도메인 내 모든경로)
                .sameSite("None") //sameSite 설정 (크롬에서 사용하려면 해당 설정이 필요함)
//                .httpOnly(false) //JS에서 쿠키 접근 가능하도록함
//                .secure(true) // HTTPS 연결에서만 쿠키 사용 sameSite 설정시 필요
                .maxAge(24 * 60 * 60)// 쿠키 유효기간 설정 (=refresh 토큰 만료주기)
                .build();
    }

    private void createAPIResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ApiResponse apiResponse = new ApiResponse<>(errorCode);
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), apiResponse);
    }

    // JSON 파일을 읽고 반환하는 역할
    private Map<String, String> getLoginInfoFromJson(HttpServletRequest request) {
        if (!"application/json".equals(request.getContentType())) {
            throw YouthException.from(ErrorCode.INVALID_REQUEST);
        }
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            System.out.println("json 읽기 실패");
            throw YouthException.from(ErrorCode.INVALID_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString.toString(), new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            System.out.println("JSON 형식에 맞지 않는 포맷");
            throw YouthException.from(ErrorCode.INVALID_REQUEST);
        }
    }
}
