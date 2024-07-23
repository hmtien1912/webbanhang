package com.vanlang.webbanhang.repository;

import com.vanlang.webbanhang.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
    List<Product> searchProductsByName(@Param("keyword") String keyword);
    List<Product> findByCategoryId(Long categoryId);
}

