package com.qiaopi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Commodity {

    private Long id;

    private String name;

    private String description;

    private String price;

    private String image;

    private String marketing;

    private String link;
}
