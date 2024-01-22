package com.ll.demo.article.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @OneToOne
    private Article article;
}