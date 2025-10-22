package com.ecommerce.user.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserAddress {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
