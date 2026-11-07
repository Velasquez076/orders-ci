package com.stores.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stores.orders.model.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the business rule "order total with discounts".
 * Written BEFORE the implementation (see docs/ESPECIFICACION.md).
 */
class OrderCalculatorTest {

    private OrderCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OrderCalculator();
    }

    private static OrderItem item(String sku, int quantity, String price) {
        return new OrderItem(sku, quantity, new BigDecimal(price));
    }

    @Test
    void CP01_subtotalAddsQuantityTimesPriceOfEachItem() {
        List<OrderItem> items = List.of(item("RICE", 2, "10000"), item("OIL", 3, "5000"));

        assertThat(calculator.calculateSubtotal(items)).isEqualByComparingTo("35000");
    }

    @Test
    void CP02_withoutDiscountTotalEqualsSubtotal() {
        List<OrderItem> items = List.of(item("RICE", 3, "10000"));

        assertThat(calculator.calculateTotal(items, BigDecimal.ZERO)).isEqualByComparingTo("30000.00");
    }

    @Test
    void CP03_percentageDiscountIsAppliedOverSubtotal() {
        List<OrderItem> items = List.of(item("RICE", 1, "100000"));

        assertThat(calculator.calculateTotal(items, new BigDecimal("10"))).isEqualByComparingTo("90000.00");
    }

    /**
     * Reproduces the bug from the case: the discount reached customers incorrectly.
     * This verifies the discount is applied ONCE over the subtotal and not
     * cumulatively (per item and then again over the total).
     */
    @Test
    void CP04_discountIsNotAppliedTwiceToTheSameOrder_reproducesCaseBug() {
        List<OrderItem> items = List.of(item("RICE", 2, "50000"), item("OIL", 3, "20000"));
        // subtotal = 100000 + 60000 = 160000 ; 10 % = 16000 ; correct total = 144000

        BigDecimal total = calculator.calculateTotal(items, new BigDecimal("10"));

        assertThat(total).isEqualByComparingTo("144000.00");
        // Result produced by the defect (double discount): 129600
        assertThat(total).isNotEqualByComparingTo("129600.00");
    }

    @Test
    void CP05_hundredPercentDiscountLeavesTotalAtZeroAndNeverNegative() {
        List<OrderItem> items = List.of(item("RICE", 2, "10000"));

        BigDecimal total = calculator.calculateTotal(items, new BigDecimal("100"));

        assertThat(total).isEqualByComparingTo("0.00");
        assertThat(total.signum()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void CP06_discountGreaterThan100IsRejected() {
        List<OrderItem> items = List.of(item("RICE", 1, "10000"));

        assertThatThrownBy(() -> calculator.calculateTotal(items, new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void CP07_negativeDiscountIsRejected() {
        List<OrderItem> items = List.of(item("RICE", 1, "10000"));

        assertThatThrownBy(() -> calculator.calculateTotal(items, new BigDecimal("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void CP08_resultIsRoundedToTwoDecimals() {
        // 3 x 3333.33 = 9999.99 ; 15 % = 1499.9985 -> 1500.00 ; total = 8499.99
        List<OrderItem> items = List.of(item("PANELA", 3, "3333.33"));

        assertThat(calculator.calculateTotal(items, new BigDecimal("15"))).isEqualByComparingTo("8499.99");
    }

    @Test
    void CP09_orderWithoutItemsIsRejected() {
        assertThatThrownBy(() -> calculator.calculateTotal(List.of(), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
