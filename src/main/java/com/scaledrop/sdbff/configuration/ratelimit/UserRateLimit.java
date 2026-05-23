package com.scaledrop.sdbff.configuration.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRateLimit {

  int capacity() default 20;

  int refillTokens() default 20;

  int refillMinutes() default 1;
}
