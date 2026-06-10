#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DYNAMODB_DIR="${ROOT_DIR}/dynamodb-local"
ARCHIVE="${DYNAMODB_DIR}/dynamodb_local_latest.tar.gz"

mkdir -p "${DYNAMODB_DIR}"

if [[ ! -f "${DYNAMODB_DIR}/DynamoDBLocal.jar" ]]; then
	curl --fail --location \
		--output "${ARCHIVE}" \
		https://d1ni2b6xgvw0s0.cloudfront.net/dynamodb_local_latest.tar.gz
	tar -xzf "${ARCHIVE}" -C "${DYNAMODB_DIR}"
fi

exec java \
	-Djava.library.path="${DYNAMODB_DIR}/DynamoDBLocal_lib" \
	-jar "${DYNAMODB_DIR}/DynamoDBLocal.jar" \
	-sharedDb \
	-dbPath "${DYNAMODB_DIR}"
