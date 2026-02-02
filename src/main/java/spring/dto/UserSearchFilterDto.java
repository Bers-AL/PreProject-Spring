package spring.dto;

import lombok.Data;

@Data
public class UserSearchFilterDto {
    private String email;
    private String firstName;
    private String lastName;

    private String brand;
    private String model;
    private Integer year;
    private String color;
}