package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Email
    @NotBlank
    @Schema(example = "salem.shah.dev@gmail.com")
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(example = "123456")
    private String password;
}
