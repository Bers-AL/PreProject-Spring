package spring.dto;

import lombok.Data;

@Data
public class UserFormDto {
    private Long id;          // null при создании
    private String firstName;
    private String lastName;
    private String email;
}