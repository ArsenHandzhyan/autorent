package ru.anapa.autorent.dto;

import lombok.Data;

@Data
public class UserSummaryDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}