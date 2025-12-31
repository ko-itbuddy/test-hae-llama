package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ProductService productService;

    @Mock
    private PriceService priceService;

    @Mock
    private AiService aiService;

    @Test
    void testGetDiscountedPriceInUsd_ValidInput() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        // 10% discount
        BigDecimal discountRate = new BigDecimal("0.10");
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, "Product A", originalPrice)));
        when(currencyConverter.convertToUsd(originalPrice)).thenReturn(originalPrice);
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        assertEquals(expectedDiscountedPrice, discountedPriceInUsd);
        verify(productRepository, times(1)).findById(productId);
        verify(currencyConverter, times(1)).convertToUsd(originalPrice);
    }

    @Test
    void testGetDiscountedPriceInUsd_WithValidProductIdAndPrices() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discountRate = new BigDecimal("0.20");
        BigDecimal exchangeRate = new BigDecimal("1.50");
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, originalPrice)));
        when(discountService.getDiscountRateForProduct(productId)).thenReturn(discountRate);
        when(exchangeRateService.getExchangeRateToUsd()).thenReturn(exchangeRate);
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate)).multiply(exchangeRate);
        assertEquals(expectedDiscountedPrice, discountedPriceInUsd);
        verify(productRepository, times(1)).findById(productId);
        verify(discountService, times(1)).getDiscountRateForProduct(productId);
        verify(exchangeRateService, times(1)).getExchangeRateToUsd();
    }

    @Test
    void testGetDiscountedPriceInUsd() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        // 10% discount
        BigDecimal discountRate = new BigDecimal("0.10");
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, originalPrice)));
        // Assume exchange rate to USD is 1.2
        when(priceService.getExchangeRateToUsd()).thenReturn(new BigDecimal("1.2"));
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        assertEquals(expectedDiscountedPrice.multiply(BigDecimal.valueOf(1.2)), discountedPriceInUsd);
        // Verify interactions
        verify(productRepository, times(1)).findById(productId);
        verify(priceService, times(1)).getExchangeRateToUsd();
    }

    @Test
    void testGetDiscountedPriceInUsd() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        // 10% discount
        BigDecimal discountRate = new BigDecimal("0.10");
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, "Product Name", originalPrice)));
        // 1 USD = 1.2 of the product's currency
        when(exchangeRateClient.getExchangeRateToUsd()).thenReturn(1.2);
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        assertEquals(expectedDiscountedPrice.multiply(new BigDecimal("1.2")), discountedPriceInUsd);
        // Verify interactions
        verify(productRepository, times(1)).findById(productId);
        verify(exchangeRateClient, times(1)).getExchangeRateToUsd();
    }

    @Test
    void testGetDiscountedPriceInUsd_InvalidProductId() {
        // Arrange
        Long invalidProductId = -1L;
        BigDecimal expectedPrice = null;
        when(productRepository.findById(invalidProductId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getDiscountedPriceInUsd(invalidProductId));
        verify(productRepository, times(1)).findById(invalidProductId);
    }

    @Test
    void testGetDiscountedPriceInUsd_ProductNotFound() {
        // Arrange
        // Assuming this product ID does not exist in the database
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getDiscountedPriceInUsd(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetDiscountedPriceInUsd() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        // 20% discount
        BigDecimal discountRate = new BigDecimal("0.20");
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, originalPrice)));
        when(priceConverter.convertToUsd(originalPrice)).thenReturn(expectedDiscountedPrice);
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        assertEquals(expectedDiscountedPrice, discountedPriceInUsd);
        verify(productRepository, times(1)).findById(productId);
        verify(priceConverter, times(1)).convertToUsd(originalPrice);
    }

    @Test
    void getDiscountedPriceInUsd_productNotFound_throwsProductNotFoundException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getDiscountedPriceInUsd(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetDiscountedPriceInUsd_ProductNotFound_ThrowsException() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getDiscountedPriceInUsd(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetDiscountedPriceInUsd_NullProductId_ThrowsIllegalArgumentException() {
        Long productId = null;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    void testGetDiscountedPriceInUsd_ZeroPrice_ReturnsZeroDiscountedPrice() {
        Long productId = 1L;
        BigDecimal price = BigDecimal.ZERO;
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testGetDiscountedPriceInUsd_NegativePrice_ThrowsIllegalArgumentException() {
        Long productId = 1L;
        BigDecimal price = new BigDecimal("-10.0");
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    void testGetDiscountedPriceInUsd_ZeroExchangeRate_ThrowsIllegalArgumentException() {
        Long productId = 1L;
        BigDecimal price = new BigDecimal("10.0");
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRateToUsd()).thenReturn(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    void testGetDiscountedPriceInUsd_NegativeExchangeRate_ThrowsIllegalArgumentException() {
        Long productId = 1L;
        BigDecimal price = new BigDecimal("10.0");
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRateToUsd()).thenReturn(new BigDecimal("-1.0"));
        assertThrows(IllegalArgumentException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    void testGetDiscountedPriceInUsd_NullDiscountValue_ReturnsPriceInUsd() {
        Long productId = 1L;
        BigDecimal price = new BigDecimal("10.0");
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BigDecimal exchangeRate = new BigDecimal("2.0");
        when(exchangeRateClient.getExchangeRateToUsd()).thenReturn(exchangeRate);
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);
        assertEquals(price.multiply(exchangeRate), result);
    }

    @Test
    void testGetDiscountedPriceInUsd_InvalidDiscountRate_ThrowsIllegalArgumentException() {
        Long productId = 1L;
        BigDecimal price = new BigDecimal("10.0");
        Product product = new Product();
        product.setPrice(price);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BigDecimal exchangeRate = new BigDecimal("2.0");
        when(exchangeRateClient.getExchangeRateToUsd()).thenReturn(exchangeRate);
        // invalid, should be between 0 and 1
        BigDecimal discountRate = new BigDecimal("1.5");
        product.setDiscountRate(discountRate);
        assertThrows(IllegalArgumentException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    void testGetDiscountedPriceInUsd() {
        // Arrange
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        // 10% discount
        BigDecimal discountRate = new BigDecimal("0.10");
        BigDecimal expectedDiscountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(productId, "Product A", originalPrice)));
        // Assume 1:1 exchange rate for simplicity
        when(exchangeRateClient.getExchangeRateToUsd(anyString())).thenReturn(1.0);
        // Act
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // Assert
        assertEquals(expectedDiscountedPrice, discountedPriceInUsd);
        verify(productRepository, times(1)).findById(productId);
        verify(exchangeRateClient, times(1)).getExchangeRateToUsd(anyString());
    }

    @Test
    void testGetDiscountedPriceInUsd_ProductNotFound_ThrowsException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getDiscountedPriceInUsd(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetDiscountedPriceInUsd_ProductFound_ReturnsDiscountedPrice() {
        Long productId = 1L;
        Product product = new Product();
        product.setPrice(new BigDecimal("100.00"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(priceService.applyDiscount(any(BigDecimal.class))).thenReturn(new BigDecimal("90.00"));
        BigDecimal discountedPrice = productService.getDiscountedPriceInUsd(productId);
        assertEquals(new BigDecimal("90.00"), discountedPrice);
        verify(productRepository, times(1)).findById(productId);
        verify(priceService, times(1)).applyDiscount(any(BigDecimal.class));
    }

    @Test
    void testGetDiscountedPriceInUsd_ProductFoundWithZeroPrice_ReturnsZero() {
        Long productId = 2L;
        Product product = new Product();
        product.setPrice(new BigDecimal("0.00"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BigDecimal discountedPrice = productService.getDiscountedPriceInUsd(productId);
        assertEquals(new BigDecimal("0.00"), discountedPrice);
        verify(productRepository, times(1)).findById(productId);
        verify(priceService, never()).applyDiscount(any(BigDecimal.class));
    }

    @BeforeEach
    public void setUp() {
        // Initialization code if needed before each test
    }
}
