package spring.dto;

import lombok.Data;

@Data
public class UserViewDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}