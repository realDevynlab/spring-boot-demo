package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
// @Entity
// @Table(name = "privileges")
public class Privilege extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /* @ManyToMany(mappedBy = "privileges")
    private Set<Role> roles; */

}
