package spring.dto;

import lombok.Data;

@Data
public class CarFormDto {
    private String brand;
    private String model;
    private Integer year;     // nullable
    private String color;     // nullable
}