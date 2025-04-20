package com.example.ecommerce.ecommerce.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "admin")
@DiscriminatorValue("ADMIN")
public class Admin extends Users {
}
