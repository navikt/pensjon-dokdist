#!/bin/bash
vault --version || (
  echo "ERROR: You need to install the Vault CLI on your machine: https://www.vaultproject.io/downloads.html" && exit 1
) || exit 1
jq --version || (
  echo "ERROR: You need to install the jq CLI tool on your machine: https://stedolan.github.io/jq/" && exit 1
) || exit 1
which base64 || (
  echo "ERROR: You need to install the base64 tool on your machine. (brew install base64 on macOS)" && exit 1
) || exit 1

export VAULT_ADDR=https://vault.adeo.no

# Uncomment these lines if you're running on MacOS (which uses the ScaleFT proxy to connect to Vault)
# export HTTPS_PROXY="socks5://localhost:14122"
# export VAULT_SKIP_VERIFY="true"

while true; do
	NAME="$(vault token lookup -format=json | jq '.data.display_name' -r; exit ${PIPESTATUS[0]})"
  ret=${PIPESTATUS[0]}
  if [ $ret -ne 0 ]; then
    echo "Looks like you are not logged in to Vault."

    read -p "Do you want to log in? (y/n) " -n 1 -r
    echo    # (optional) move to a new line
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
      vault login -method=oidc -no-print
    else
      echo "Could not log in to Vault. Aborting."
      exit 1
    fi
  else
    break;
  fi
done

echo "Logged in to Vault as $NAME. Fetching secrets..."

mkdir -p secrets/oidc
mkdir -p secrets/serviceuser
mkdir -p secrets/truststore

vault kv get -field client_secret credential/dev/oidc-openam-pensjon-dokdist-localhost > secrets/oidc/client_secret
vault kv get -field username serviceuser/dev/srvpensjon-dokdist > secrets/serviceuser/username
vault kv get -field password serviceuser/dev/srvpensjon-dokdist > secrets/serviceuser/password
vault kv get -field keystore certificate/dev/nav-truststore | base64 --decode > secrets/truststore/truststore.jts
vault kv get -field keystorepassword certificate/dev/nav-truststore > secrets/truststore/password

echo "All secrets are fetched and stored in the \"secrets\" folder."
