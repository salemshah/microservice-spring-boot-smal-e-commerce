//package com.ecommerce.order.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.validation.Validator;
//import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
//
///**
// * Shared Validator configuration for all microservices.
// * Prevents conflicts between Web and Bus validator beans.
// */
//@Configuration
//public class ValidationConfig {
//
//    @Bean
//    @Primary
//    public Validator primaryValidator() {
//        return new LocalValidatorFactoryBean();
//    }
//}
