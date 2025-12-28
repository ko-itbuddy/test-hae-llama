package com.example.demo.repository;

import com.example.demo.domain.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findProductsExpensiveThan(BigDecimal price);
}
