#!/bin/bash

KUBE_CLUSTER="dev-gcp"

jq --version || (
  echo "ERROR: You need to install the jq CLI tool on your machine: https://stedolan.github.io/jq/" && exit 1
) || exit 1
which nais || (
  echo "ERROR: You need to install the nais CLI tool on your machine: https://doc.nais.io/operate/how-to/naisdevice/nais-cli/" && exit 1
) || exit 1
mkdir -p secrets

nais secret get "azure-pensjon-dokdist-lokal" -t pensjonsbrev --environment $KUBE_CLUSTER --with-values --reason "local development" --output json | jq '.data | from_entries' > secrets/azuread.json
echo "All secrets are fetched and stored in the \"secrets\" folder."
