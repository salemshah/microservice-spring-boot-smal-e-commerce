package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserAddressMapper userAddressMapper;

    /**
     * Map a general UserRequestDTO (used for admin/user profile updates).
     */
    public User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phon(dto.getPhon())
                .password(dto.getPassword())
                .userRole(UserRole.valueOf(dto.getUserRole().toUpperCase()))
                .build();

        // Map addresses
        if (dto.getAddresses() != null) {
            List<UserAddress> userAddressList = dto.getAddresses()
                    .stream()
                    .map(userAddressMapper::toEntity)
                    .toList();
            user.setAddresses(userAddressList);
        }

        return user;
    }

    /**
     * Map a RegisterRequestDTO to User (used during registration).
     * Handles password encoding and default role.
     */
    public User toEntityRegister(RegisterRequestDTO dto) {
        if (dto == null) return null;

        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword()) // plain here, service encodes
                .build();
    }


    /**
     * Convert User to full UserResponseDTO (with addresses).
     */
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhon(user.getPhon());
        dto.setEmail(user.getEmail());
        dto.setUserRole(user.getUserRole());

        if (user.getAddresses() != null) {
            List<UserAddressDTO> addressDTOList = user.getAddresses()
                    .stream()
                    .map(userAddressMapper::toDTO)
                    .toList();
            dto.setAddresses(addressDTOList);
        }

        return dto;
    }

    public UserResponseDTO toAuthResponseDTO(User user) {
        if (user == null) return null;

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .build();
    }
}
