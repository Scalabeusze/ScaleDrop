# ScaleDrop documentation 

## Local Development Environment

This Docker Compose setup provides a complete local development environment with:

* **PostgreSQL** – primary database
* **Redis** – caching layer
* **LocalStack** – AWS services emulation (S3, SNS, SQS)
* **Resource initializer** – automatically provisions AWS resources

---

### Getting Started

#### 1. Start core services

```bash
docker compose up -d
```

This starts:

* Redis
* PostgreSQL

---

#### 2. Start full environment (with AWS emulation)

```bash
docker compose --profile full up -d
```

This additionally starts:

* LocalStack
* Resource setup container

---

### Services Overview

#### Redis

* **Port:** `6379`
* **Password:** `password`

Used for caching and temporary data storage.

---

#### PostgreSQL

* **Port:** `5432`
* **Password:** `password`

Default database instance for the application.

---

#### LocalStack

* **Endpoint:** `http://localhost:4566`
* **Region:** `eu-west-1`
* **Services enabled:**

    * S3
    * SNS
    * SQS

Provides a fully local AWS-like environment.

---

### Setup Resources (init container)

Runs automatically when using the `full` profile.

Responsible for provisioning:

#### S3

* Bucket: `example-bucket`

#### SNS

* Topic: `file-updates-topic`

#### SQS

* Queue: `file-event`

#### Integration

* SQS queue is subscribed to SNS topic
* Proper IAM policy is applied to allow SNS → SQS communication

---

#### AWS Credentials (Local Only)

```text
AWS_ACCESS_KEY_ID=dummyaccess
AWS_SECRET_ACCESS_KEY=dummysecret
AWS_DEFAULT_REGION=eu-west-1
```

These are dummy credentials used only for LocalStack.

---

### Testing the Setup

#### Send a message to SNS:

```bash
aws --endpoint-url=http://localhost:4566 sns publish \
  --topic-arn arn:aws:sns:eu-west-1:000000000000:file-updates-topic \
  --message "test message"
```

#### Receive message from SQS:

```bash
aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://localhost:4566/000000000000/file-event
```

---

### Notes

* The `setup-resources` container waits ~10 seconds for LocalStack to start before provisioning resources.
* Resource names and ARNs are deterministic in LocalStack (`account id = 000000000000`).
* All data is ephemeral unless volumes are persisted.

---

### Cleanup

```bash
docker compose down -v
```

Removes all containers and associated volumes.

Alternatively, you can add the `docker-kill.sh` file to your bin directory and run:
```
docker-kill.sh
```

This will stop all containers currently running on your machine, so be cautious when using it.

---

### Tips

* You can connect your application to LocalStack using:

  ```text
  http://localhost:4566
  ```
* For Spring Boot, configure:

  ```yaml
  cloud:
    aws:
      endpoint: http://localhost:4566
  ```

---

### Profiles

| Profile | Description                          |
| ------- | ------------------------------------ |
| default | Redis + PostgreSQL                   |
| full    | Adds LocalStack + AWS resource setup |

