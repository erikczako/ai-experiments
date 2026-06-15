#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
install_dir="${root_dir}/.dynamodb-local"
archive="${install_dir}/dynamodb_local_latest.tar.gz"

mkdir -p "${install_dir}"
if [[ ! -f "${install_dir}/DynamoDBLocal.jar" ]]; then
	curl --fail --location --silent --show-error \
		"https://d1ni2b6xgvw0s0.cloudfront.net/dynamodb_local_latest.tar.gz" \
		--output "${archive}"
	tar -xzf "${archive}" -C "${install_dir}"
fi

cd "${install_dir}"
exec java \
	--enable-native-access=ALL-UNNAMED \
	-Djava.library.path="${install_dir}/DynamoDBLocal_lib" \
	-jar "${install_dir}/DynamoDBLocal.jar" \
	-sharedDb \
	-dbPath "${install_dir}"
