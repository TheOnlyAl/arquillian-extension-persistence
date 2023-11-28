package org.jboss.arquillian.integration.persistence.example;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "simple")
public class SimpleEntry implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
}
