package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.UserAddressDTO;
import com.ecommerce.user.entity.UserAddress;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper {

    public UserAddress toEntity(UserAddressDTO dto) {
        if (dto == null) return null;
        UserAddress address = new UserAddress();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        return address;
    }

    public UserAddressDTO toDTO(UserAddress address) {
        if (address == null) return null;
        UserAddressDTO dto = new UserAddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());
        return dto;
    }
}
