package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TrendAnalyzer trendAnalyzer;

    @InjectMocks
    private AiService aiService;

    @Mock
    private ProductRepository mockRepository;

    @Mock
    private MarketClient mockClient;

    @Mock
    private TrendAnalysisClient trendAnalysisClient;

    @Mock
    private DependencyRepository dependencyRepository;

    @Test
    void testAnalyzeProductTrend_Success() throws Exception {
        // Arrange
        String productName = "SampleProduct";
        String expectedTrend = "Increasing";
        when(productRepository.findByName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalysisClient.analyze(any(Product.class))).thenReturn(expectedTrend);
        // Act
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.get();
        // Assert
        assertEquals(expectedTrend, result);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalysisClient, times(1)).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_Success() {
        // Arrange
        String productName = "Smartphone";
        String expectedTrendAnalysis = "Increasing trend";
        when(productRepository.findByName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalyzer.analyze(any(Product.class))).thenReturn(expectedTrendAnalysis);
        // Act
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.join();
        // Assert
        assertEquals(expectedTrendAnalysis, result);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalyzer, times(1)).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ProductNotFound() {
        // Arrange
        String productName = "Smartphone";
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        // Act & Assert
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        ExecutionException exception = assertThrows(ExecutionException.class, () -> resultFuture.join());
        assertTrue(exception.getCause() instanceof ProductNotFoundException);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalyzer, never()).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ShouldReturnTrendAnalysisResult() throws ExecutionException, InterruptedException {
        // Given
        String productName = "smartphones";
        String expectedTrendResult = "Increasing trend";
        when(mockRepository.findProductData(productName)).thenReturn();
        when(mockClient.fetchMarketData()).thenReturn();
        // When
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.get();
        // Then
        assertEquals(expectedTrendResult, result);
        verify(mockRepository, times(1)).findProductData(productName);
        verify(mockClient, times(1)).fetchMarketData();
    }

    @Test
    void testAnalyzeProductTrend_Success() {
        // Given
        String productName = "Smartphone";
        CompletableFuture<String> expectedFuture = CompletableFuture.completedFuture("Increasing trend");
        when(productRepository.findByName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalysisClient.analyzeTrend(any(Product.class))).thenReturn(expectedFuture);
        // When
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.join();
        // Then
        assertEquals("Increasing trend", result);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalysisClient, times(1)).analyzeTrend(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ProductNotFound() {
        // Given
        String productName = "NonExistentProduct";
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        // When & Then
        assertThrows(ProductNotFoundException.class, () -> aiService.analyzeProductTrend(productName).join());
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalysisClient, never()).analyzeTrend(any(Product.class));
    }

    @Test
    @ExtendWith(MockitoExtension.class)
    public void testAnalyzeProductTrend_WithNullProductName() {
        // Arrange
        String productName = null;
        CompletableFuture<String> expectedFuture = CompletableFuture.completedFuture("No data available");
        when(aiRepository.analyzeTrend(productName)).thenReturn(expectedFuture);
        // Act
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        // Assert
        assertEquals(expectedFuture.join(), resultFuture.join());
        verify(aiRepository, times(1)).analyzeTrend(productName);
    }

    @Test
    void testAnalyzeProductTrendWithNullInput() {
        // Arrange
        String productName = null;
        // Act & Assert
        assertThrows(NullPointerException.class, () -> aiService.analyzeProductTrend(productName));
    }

    @Test
    void testAnalyzeProductTrend_WithNullProductName_CompletesExceptionally() {
        // Arrange
        String productName = null;
        // Act & Assert
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        assertThatThrownBy(() -> result.join()).isInstanceOf(IllegalArgumentException.class).hasMessage("Product name cannot be null");
    }

    @Test
    void testAnalyzeProductTrend_Success() throws Exception {
        // Given
        String productName = "ExampleProduct";
        String expectedAnalysisResult = "Positive Trend";
        when(productRepository.findByName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalyzer.analyze(any(Product.class))).thenReturn(expectedAnalysisResult);
        // When
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.get();
        // Then
        assertEquals(expectedAnalysisResult, result);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalyzer, times(1)).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ProductNotFound() throws Exception {
        // Given
        String productName = "NonExistentProduct";
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        // When & Then
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        ExecutionException exception = assertThrows(ExecutionException.class, resultFuture::get);
        assertTrue(exception.getCause() instanceof ProductNotFoundException);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalyzer, never()).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ProductNotFound() {
        // Arrange
        String productName = "NonExistentProduct";
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        // Act & Assert
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        assertThat(result.join()).isEqualTo("Product not found");
        verify(productRepository, times(1)).findByName(productName);
    }

    @Test
    void testAnalyzeProductTrendWithEmptyProductName() {
        // Arrange
        when(aiRepository.findByName(anyString())).thenReturn(Optional.empty());
        // Act
        CompletableFuture<String> result = aiService.analyzeProductTrend("");
        // Assert
        verify(aiRepository, times(1)).findByName("");
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void testAnalyzeProductTrend_WithNullProductName_CompletesExceptionally() {
        // Arrange
        String productName = null;
        // Act & Assert
        CompletableFuture<String> future = aiService.analyzeProductTrend(productName);
        assertThatThrownBy(() -> future.join()).isInstanceOf(IllegalArgumentException.class).hasMessage("Product name cannot be null");
    }

    @Test
    void testAnalyzeProductTrend_Success() {
        // Arrange
        String productName = "Test Product";
        String expectedTrend = "Increasing";
        when(productRepository.findByName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalysisClient.analyze(any(Product.class))).thenReturn(expectedTrend);
        // Act
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        String result = resultFuture.join();
        // Assert
        assertEquals(expectedTrend, result);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalysisClient, times(1)).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_ProductNotFound() {
        // Arrange
        String productName = "Nonexistent Product";
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        // Act & Assert
        CompletableFuture<String> resultFuture = aiService.analyzeProductTrend(productName);
        ExecutionException exception = assertThrows(ExecutionException.class, () -> resultFuture.join());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(productRepository, times(1)).findByName(productName);
        verify(trendAnalysisClient, never()).analyze(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_WithNonExistentProductName() {
        // Arrange
        String productName = "NonExistentProduct";
        when(productRepository.existsByName(productName)).thenReturn(false);
        // Act & Assert
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        assertThat(result.join()).isEqualTo("Product not found");
        verify(productRepository, times(1)).existsByName(productName);
    }

    @Test
    void testAnalyzeProductTrendWithEmptyProductName() {
        // Arrange
        String productName = "";
        CompletableFuture<String> expectedResult = CompletableFuture.completedFuture("No data available");
        when(aiRepository.analyzeProductTrend(productName)).thenReturn(expectedResult);
        // Act
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        // Assert
        assertEquals(expectedResult, result);
        verify(aiRepository, times(1)).analyzeProductTrend(productName);
    }

    @Test
    void testAnalyzeProductTrend_WithNullProductName_CompletesExceptionally() {
        // Given
        String productName = null;
        // When
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        // Then
        assertThatThrownBy(() -> result.join()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAnalyzeProductTrend_success() throws Exception {
        // Arrange
        String productName = "Sample Product";
        when(productRepository.findByProductName(productName)).thenReturn(Optional.of(new Product()));
        when(trendAnalysisClient.analyzeTrend(any(Product.class))).thenReturn("Increasing");
        // Act
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        // Assert
        assertEquals("Increasing", result.get());
        verify(productRepository, times(1)).findByProductName(productName);
        verify(trendAnalysisClient, times(1)).analyzeTrend(any(Product.class));
    }

    @Test
    void testAnalyzeProductTrend_productNotFound() throws Exception {
        // Arrange
        String productName = "Non-existent Product";
        when(productRepository.findByProductName(productName)).thenReturn(Optional.empty());
        // Act & Assert
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        ExecutionException exception = assertThrows(ExecutionException.class, () -> result.get());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(productRepository, times(1)).findByProductName(productName);
        verify(trendAnalysisClient, never()).analyzeTrend(any(Product.class));
    }

    @BeforeEach
    public void setUp() {
        // Initialization logic if needed
    }
}
