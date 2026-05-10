package com.scaledrop.sdbff.application.port.in.iam;

import org.springframework.web.servlet.view.RedirectView;

public interface GoogleLoginUseCase {
  RedirectView googleLogin();
}
