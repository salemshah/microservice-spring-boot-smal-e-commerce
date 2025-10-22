package com.ecommerce.user.entity;

import com.ecommerce.user.enums.UserRole;
//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "addresses") // Prevents infinite recursion when printing User -> addresses -> User
@EqualsAndHashCode(exclude = "addresses")// Prevents recursion when comparing two User objects (User -> addresses -> User)
@Document(collection = "users") // Tells Spring Data MongoDB to store this class in the "users" collection
public class User {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String phon;
    private UserRole userRole;

    @Indexed(unique = true)
    private String email;
    private String password;
    private List<UserAddress> addresses;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
