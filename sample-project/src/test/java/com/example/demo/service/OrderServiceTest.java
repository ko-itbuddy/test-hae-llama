package com.example.demo.service

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;
import java.math.BigDecimal;
import com.example.demo.domain.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;


    @Test
    @DisplayName("Success: placeOrder")
    void testPlaceOrder() {
        <|im_start|>
GivenTHEN
GivenThen


Given("WhenGiven then given then given then given given given given given given given given given given given given given given given given given given given given given given given given given given given given given given given
    }
}