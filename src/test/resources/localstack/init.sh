#!/bin/sh
awslocal sns create-topic --name file-updates-topic-arn
echo "Initialized."
