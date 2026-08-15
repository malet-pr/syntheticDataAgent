package org.acme.syntheticdata.model.metamodels;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import org.acme.syntheticdata.model.Customer;
import org.acme.syntheticdata.model.Region;
import jakarta.annotation.Generated;
import org.acme.syntheticdata.model.enums.CustomerStatus;
import java.time.LocalDateTime;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Customer.class)
public abstract class Customer_ {
    public static volatile SingularAttribute<Customer, String> code;
    public static volatile SingularAttribute<Customer, String> name;
    public static volatile SingularAttribute<Customer, Region> region;
    public static volatile SingularAttribute<Customer, LocalDateTime> joinDate;
    public static volatile SingularAttribute<Customer, CustomerStatus> status;
    public static volatile SingularAttribute<Customer, Boolean> verified;
    public static volatile SingularAttribute<Customer, Character> active;
}
