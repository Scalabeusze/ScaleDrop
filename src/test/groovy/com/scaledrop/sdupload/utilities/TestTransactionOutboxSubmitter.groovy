package com.scaledrop.sdupload.utilities

import com.google.common.util.concurrent.MoreExecutors
import com.gruelbox.transactionoutbox.Submitter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class TestTransactionOutboxSubmitter {

  @Bean
  @Primary
  Submitter submitter() {
    return Submitter.withExecutor(MoreExecutors.directExecutor())
  }
}
