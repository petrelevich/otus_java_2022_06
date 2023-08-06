package ru.otus.crm.model;

import lombok.*;
import javax.persistence.*;
import java.util.Objects;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "phone")
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "number")
    private String number;

    public Phone(Long id, String number) {
        this.id = id;
        this.number = number;
    }
}
