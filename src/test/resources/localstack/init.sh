#!/bin/sh
awslocal sqs create-queue --queue-name file-updates-queue-url
echo "Initialized."
