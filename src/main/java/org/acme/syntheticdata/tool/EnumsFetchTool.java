package org.acme.syntheticdata.tool;

import org.acme.syntheticdata.model.enums.CustomerStatus;
import org.acme.syntheticdata.model.enums.InventoryStatus;
import org.acme.syntheticdata.model.enums.OrderStatus;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class EnumsFetchTool {

    @Tool(name = "customerStatus",description = "Returns a list of all customer status values")
    public static List<String> customerStatus() {
        List<String> customerStatus = new ArrayList<>();
        for (CustomerStatus value : CustomerStatus.values()) {
            customerStatus.add(value.name());
        }
        return customerStatus;
    }

    @Tool(name = "inventoryStatus",description = "Returns a list of all inventory status values")
    public static List<String> inventoryStatus() {
        List<String> inventoryStatus = new ArrayList<>();
        for (InventoryStatus value : InventoryStatus.values()) {
            inventoryStatus.add(value.name());
        }
        return inventoryStatus;
    }

    @Tool(name = "orderStatus",description = "Returns a list of all order status values")
    public static List<String>  orderStatus() {
        List<String> orderStatus = new ArrayList<>();
        for (OrderStatus value : OrderStatus.values()) {
            orderStatus.add(value.name());
        }
        return orderStatus;
    }


}
