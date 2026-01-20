package com.example.demo.repository.impl;

import com.example.demo.domain.Product;
import com.example.demo.domain.QProduct;
import com.example.demo.repository.ProductRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> findProductsExpensiveThan(BigDecimal price) {
        QProduct product = QProduct.product;
        
        return queryFactory.selectFrom(product)
                .where(product.price.gt(price))
                .fetch();
    }
}
