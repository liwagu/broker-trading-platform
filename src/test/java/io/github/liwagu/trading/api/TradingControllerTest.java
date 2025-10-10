package io.github.liwagu.trading.api;

import io.github.liwagu.trading.domain.MarketDataService;
import io.github.liwagu.trading.domain.OrderStatus;
import io.github.liwagu.trading.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The class contains a limited set of tests for the trading flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TradingControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BuyingPowerRepository buyingPowerRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MarketDataService marketDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        buyingPowerRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        BuyingPowerEntity buyingPowerEntity = new BuyingPowerEntity("portfolio-id-1", new BigDecimal("5000.00"));
        buyingPowerRepository.save(buyingPowerEntity);
    }

    @Test
    public void shouldCreateBuyOrderSuccessfully() throws Exception {
        Map<String, String> object = new HashMap<>();
        object.put("portfolioId", "portfolio-id-1");
        object.put("isin", "US67066G1040");
        object.put("side", "BUY");
        object.put("quantity", "10.00");

        createOrder(objectMapper.writeValueAsString(object))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.portfolioId").value("portfolio-id-1"))
                .andExpect(jsonPath("$.isin").value("US67066G1040"));

        verifyBuyingPower("portfolio-id-1", new BigDecimal("4000.00"));
        verifyInventory("portfolio-id-1", "US67066G1040", new BigDecimal("10.00"));
    }

    @Test
    public void shouldGetOrderById() throws Exception {
        Map<String, String> object = new HashMap<>();
        object.put("portfolioId", "portfolio-id-1");
        object.put("isin", "US67066G1040");
        object.put("side", "BUY");
        object.put("quantity", "10.00");

        String creationResponse = createOrder(objectMapper.writeValueAsString(object)).andReturn().getResponse().getContentAsString();

        long orderId = objectMapper.readTree(creationResponse).get("id").asLong();

        String response = getOrder(orderId).andReturn().getResponse().getContentAsString();
        assertEquals(creationResponse, response);
    }

    @Test
    public void shouldCancelBuyOrder() throws Exception {
        Map<String, String> object = new HashMap<>();
        object.put("portfolioId", "portfolio-id-1");
        object.put("isin", "US67066G1040");
        object.put("side", "BUY");
        object.put("quantity", "10.00");

        String creationResponse = createOrder(objectMapper.writeValueAsString(object)).andReturn().getResponse().getContentAsString();
        long orderId = objectMapper.readTree(creationResponse).get("id").asLong();

        cancelOrder(orderId);

        OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);
        assert orderEntity != null;
        Assertions.assertEquals(OrderStatus.CANCELLED, orderEntity.getStatus());

        verifyInventory("portfolio-id-1", "US67066G1040", new BigDecimal("0.00"));
        verifyBuyingPower("portfolio-id-1", new BigDecimal("5000.00"));
    }

    private ResultActions createOrder(String orderRequest) throws Exception {
        MockHttpServletRequestBuilder content = post("/orders")
                .content(orderRequest)
                .contentType(MediaType.APPLICATION_JSON);

        return mvc.perform(content);
    }

    private ResultActions cancelOrder(long orderId) throws Exception {
        MockHttpServletRequestBuilder content = put("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON);

        return mvc.perform(content);
    }

    private ResultActions getOrder(long orderId) throws Exception {
        MockHttpServletRequestBuilder content = get("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON);

        return mvc.perform(content);
    }

    private void verifyInventory(String portfolioId, String isin, BigDecimal expectedQuantity) {
        InventoryEntity inventoryEntity = inventoryRepository
                .findById(new InventoryEntityId(portfolioId, isin))
                .orElse(new InventoryEntity(portfolioId, isin, BigDecimal.ZERO));
        assertThat(inventoryEntity.getQuantity())
                .as("Inventory of portfolio %s and ISIN %s should be %s", portfolioId, isin, expectedQuantity)
                .isEqualByComparingTo(expectedQuantity);
    }

    private void verifyBuyingPower(String portfolioId, BigDecimal expectedAmount) {
        BuyingPowerEntity buyingPowerEntity = buyingPowerRepository
                .findById(portfolioId)
                .orElse(new BuyingPowerEntity(portfolioId, BigDecimal.ZERO));
        assertThat(buyingPowerEntity.getAmount())
                .as("Buying power of portfolio %s should be %s", portfolioId, expectedAmount)
                .isEqualByComparingTo(expectedAmount);
    }
}