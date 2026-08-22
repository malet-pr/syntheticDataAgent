package org.acme.syntheticdata.dto;

public record SeedRequest(
       int regions,
       int product_categories,
       int managers,
       int representatives,
       int products,
       int customers,
       int customer_orders,
       int orderlines
) {
}


