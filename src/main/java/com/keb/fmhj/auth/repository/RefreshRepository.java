package com.keb.fmhj.auth.repository;

import com.keb.fmhj.auth.domain.RefreshEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends CrudRepository<RefreshEntity, String> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}
