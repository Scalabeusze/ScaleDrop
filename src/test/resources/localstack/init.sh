#!/bin/sh
awslocal sqs create-queue --queue-name file-updates-queue-url
awslocal s3 mb s3://sd-fileserver-test
echo "Initialized."
