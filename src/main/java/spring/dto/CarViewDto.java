package spring.dto;

import lombok.Data;

@Data
public class CarViewDto {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String color;
}