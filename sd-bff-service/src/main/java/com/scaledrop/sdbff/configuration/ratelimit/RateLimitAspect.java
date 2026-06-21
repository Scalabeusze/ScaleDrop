package com.scaledrop.sdbff.configuration.ratelimit;

import com.scaledrop.sdbff.application.component.UserContext;
import com.scaledrop.sdbff.configuration.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

  private final RateLimitingService rateLimitingService;
  private final UserContext userContext;

  @Around("@annotation(rateLimitAnnotation)")
  public Object enforceRateLimit(ProceedingJoinPoint pjp, UserRateLimit rateLimitAnnotation)
      throws Throwable {

    UUID contextUserId = userContext.getUserId();
    String userId = (contextUserId != null) ? contextUserId.toString() : "anonymous";

    String methodName = pjp.getSignature().getName();
    String bucketKey = userId + ":" + methodName;

    Bucket bucket =
        rateLimitingService.resolveBucket(
            bucketKey,
            rateLimitAnnotation.capacity(),
            rateLimitAnnotation.refillTokens(),
            rateLimitAnnotation.refillMinutes());

    if (bucket.tryConsume(1)) {
      return pjp.proceed();
    } else {
      log.warn("[RATE-LIMIT] User {} exceeded rate limit for method: {}", userId, methodName);
      throw new RateLimitExceededException(
          "You have exhausted your API Request quota. Please try again later.");
    }
  }
}
