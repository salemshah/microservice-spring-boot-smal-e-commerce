package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAddressDTO {

    private String id;

    @NotBlank(message = "Street is required")
    @Size(max = 120, message = "Street must be at most 120 characters")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 80, message = "Country must be at most 80 characters")
    private String country;

    @Pattern(
            regexp = "^[A-Za-z0-9\\-\\s]{3,12}$",
            message = "Postal code must be 3–12 letters/digits"
    )
    @NotBlank(message = "Postal code is required")
    private String postalCode;
}
