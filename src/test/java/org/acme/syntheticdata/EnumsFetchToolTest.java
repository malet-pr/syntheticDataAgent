package org.acme.syntheticdata;

import org.acme.syntheticdata.model.enums.CustomerStatus;
import org.acme.syntheticdata.model.enums.InventoryStatus;
import org.acme.syntheticdata.model.enums.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Arrays;
import java.util.List;
import static org.acme.syntheticdata.tool.EnumsFetchTool.*;

@ExtendWith(SpringExtension.class)
class EnumsFetchToolTest {

    List<String> customerTool = Arrays.stream(CustomerStatus.values())
            .map(Enum::name)
            .toList();


    @DisplayName("Test customerStatus tool")
    @Test
    void testCustomerStatusTool() {
        List<String> toolResult = customerStatus();
        Assertions.assertEquals(customerTool, toolResult);
    }

    List<String> inventoryTool = Arrays.stream(InventoryStatus.values())
            .map(Enum::name)
            .toList();


    @DisplayName("Test inventoryStatus tool")
    @Test
    void testInventoryStatusTool() {
        List<String> toolResult = inventoryStatus();
        Assertions.assertEquals(inventoryTool, toolResult);
    }


    List<String> orderTool = Arrays.stream(OrderStatus.values())
            .map(Enum::name)
            .toList();


    @DisplayName("Test orderStatus tool")
    @Test
    void testOrderStatusTool() {
        List<String> toolResult = orderStatus();
        Assertions.assertEquals(orderTool, toolResult);
    }



}

