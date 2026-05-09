package com.scaledrop.sdbff.configuration.annotations;

import static com.scaledrop.sdbff.configuration.Constants.BASIC_AUTH;
import static com.scaledrop.sdbff.configuration.Constants.BEARER_JWT;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirement(name = BEARER_JWT)
@SecurityRequirement(name = BASIC_AUTH)
public @interface DefaultApiSecurity {}
