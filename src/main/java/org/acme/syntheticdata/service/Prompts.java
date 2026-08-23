package org.acme.syntheticdata.service;

import org.acme.syntheticdata.dto.SeedRequest;
import org.springframework.stereotype.Component;

@Component
public class Prompts {

    public static String step1(SeedRequest req) {
        if(req.representatives() == 0
                && req.managers() == 0
                && req.regions() == 0){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("""
              You are an autonomous database seeding agent.
              Use data-generation skill to generate:
              """);
        if(req.regions() > 0) sb.append("   - ").append(req.regions()).append(" regions.\n");
        if(req.managers() > 0) sb.append("   - ").append(req.managers()).append(" managers.\n");
        if(req.representatives() > 0) sb.append("   - ").append(req.representatives()).append(" representatives.\n");
        sb.append("Use data-insertion skill to insert the sql generated.");
        return sb.toString();
    }

    public static String step2(SeedRequest req) {
        if(req.products() == 0 && req.product_categories() == 0){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("""
              You are an autonomous database seeding agent.
              Use data-generation skill to generate:
              """);
        if(req.product_categories() > 0) sb.append("   - ").append(req.product_categories()).append(" product_categories.\n");
        if(req.products() > 0) sb.append("   - ").append(req.products()).append(" products.\n");
        sb.append("Use data-insertion skill to insert the sql generated.");
        return sb.toString();
    }

    public static String step3(int quantity) {
        if(quantity == 0) return null;
        String step = """
                You are an autonomous database seeding agent.
                Use data-generation skill to generate %d customers.
                Follow these directives:
                   - Customers that are not verified cannot be active.
                   - Customers in any status can be inactives.
                   - Only NEW customers can be not verified.
                Use data-insertion skill to insert the sql generated.
                """.formatted(quantity);
        return step;
    }

    public static String step4(int quantity) {
        if(quantity == 0) return null;
        String step = """
            You are an autonomous database seeding agent.
            Follow these directives:
                - Use data-generation skill to generate %d NEW customer_orders.
                - These MUST be additional unique orders for existing customers (do not re-insert or replace existing orders).
            Use data-insertion skill to insert the generated SQL.
            """.formatted(quantity);
        return step;
    }

    public static String step5(int quantity) {
        if(quantity == 0) return null;
        String step = """
            You are an autonomous database seeding agent.
            Follow these directives:
                - Use data-generation skill to generate %d NEW orderlines for the newly created orders in the previous step.
                - For each orderline: amount = product_price * quantity.
                - No more than 30 percent of the orders can have only 1 orderline.
            Use data-insertion skill to insert the generated SQL.
            """.formatted(quantity);
        return step;
    }

}
