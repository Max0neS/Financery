package com.example.financery.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "UserTable")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private int balance = 0;

}
