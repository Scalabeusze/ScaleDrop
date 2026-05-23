package com.scaledrop.sdbff.configuration.ratelimit;

import com.scaledrop.sdbff.configuration.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

  private final RateLimitingService rateLimitingService;

  @Around("@annotation(rateLimitAnnotation)")
  public Object enforceRateLimit(ProceedingJoinPoint pjp, UserRateLimit rateLimitAnnotation)
      throws Throwable {
    String userId = extractUserIdFromArgs(pjp.getArgs());

    Bucket bucket =
        rateLimitingService.resolveBucket(
            userId,
            rateLimitAnnotation.capacity(),
            rateLimitAnnotation.refillTokens(),
            rateLimitAnnotation.refillMinutes());

    if (bucket.tryConsume(1)) {
      return pjp.proceed();
    } else {
      log.warn(
          "[RATE-LIMIT] User {} exceeded rate limit for method: {}",
          userId,
          pjp.getSignature().getName());
      throw new RateLimitExceededException(
          "You have exhausted your API Request quota. Please try again later.");
    }
  }

  private String extractUserIdFromArgs(Object[] args) {
    for (Object arg : args) {
      if (arg instanceof Jwt jwt) {
        return jwt.getSubject();
      }
    }
    return "anonymous";
  }
}
