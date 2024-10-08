package com.keb.fmhj.item.domain.repository;

import com.keb.fmhj.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i " +
            "join fetch ResultItem r " +
            "on i.id=r.item.id " +
            "where r.result.id=:resultId")
    List<Item> findItemsByResultId(Long resultId);

    @Query("select i FROM Item i WHERE i.name IN :names")
    List<Item> findItemsByNamesExist(@Param("names") List<String> names);
}
