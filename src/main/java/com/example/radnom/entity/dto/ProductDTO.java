package com.example.radnom.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private int id;
    private String productName;
    private double price;
    private String description;
    private String category;
    private String imageUrl;

}
