package com.scaledrop.sdupload.configuration.transactionoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gruelbox.transactionoutbox.DefaultPersistor;
import com.gruelbox.transactionoutbox.Dialect;
import com.gruelbox.transactionoutbox.Submitter;
import com.gruelbox.transactionoutbox.TransactionOutbox;
import com.gruelbox.transactionoutbox.TransactionOutbox.TransactionOutboxBuilder;
import com.gruelbox.transactionoutbox.jackson.JacksonInvocationSerializer;
import com.gruelbox.transactionoutbox.spring.SpringInstantiator;
import com.gruelbox.transactionoutbox.spring.SpringTransactionManager;
import com.scaledrop.sdupload.configuration.transactionoutbox.TransactionOutboxProperties.SchedulingProperties;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Import({SpringInstantiator.class, SpringTransactionManager.class})
public class TransactionOutboxConfiguration {

  private final SpringTransactionManager transactionManager;
  private final SpringInstantiator instantiator;
  private final ObjectMapper objectMapper;
  private final TransactionOutboxProperties properties;
  private final Clock clock;

  @Bean
  public TransactionOutbox transactionOutbox(Optional<Submitter> submitter) {
    return transactionOutbox(properties.getPublishEvent(), submitter.orElseGet(this::submitter));
  }

  private Submitter submitter() {
    SemaphoreBoundedExecutor boundedExecutor = new SemaphoreBoundedExecutor(Executors.newVirtualThreadPerTaskExecutor(), properties.getMaxConcurrentTasks());
    return Submitter.withExecutor(boundedExecutor);
  }

  private TransactionOutbox transactionOutbox(SchedulingProperties schedulingProperties, Submitter submitter) {
    DefaultPersistor persistor = DefaultPersistor.builder()
        .dialect(Dialect.POSTGRESQL_9)
        .tableName(schedulingProperties.getTableName())
        .migrate(false)
        .serializer(JacksonInvocationSerializer.builder().mapper(objectMapper).build())
        .build();

    TransactionOutboxBuilder builder = TransactionOutbox.builder()
        .instantiator(instantiator)
        .transactionManager(transactionManager)
        .attemptFrequency(schedulingProperties.getAttemptFrequency())
        .blockAfterAttempts(schedulingProperties.getBlockAfterAttempts())
        .flushBatchSize(schedulingProperties.getFlushBatchSize())
        .persistor(persistor)
        .serializeMdc(true)
        .submitter(submitter)
        .clockProvider(() -> clock);

    return builder.build();
  }
}
