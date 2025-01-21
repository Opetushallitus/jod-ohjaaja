#!/bin/bash
set -e -o pipefail

if [[ -n $AWS_SESSION_TOKEN && -n $DEV_BUCKET ]]; then
  mkdir -p ./tmp/data
  mkdir -p ./.run
  aws s3 cp s3://${DEV_BUCKET}/jod-ohjaaja-backend/application-local.yml .
  aws s3 cp s3://${DEV_BUCKET}/jod-ohjaaja-backend/jod-ohjaaja-bootRun.run.xml .run/
else
  echo "WARN: Skipping data and configuration download, missing AWS credentials or DEV_BUCKET" >&2
fi
