#!/bin/bash
# This script should be run manually whenever we make changes to the OpenAM config


# curl --request POST --data '{"envclass": "t", "agentname": "pselv-localhost", "agentpassword": "123123", "redirectURLs": ["http://localhost:9080/pselv/oidc/callback"]}' https://amag.nais.preprod.local/createagent -iv

echo "Your ident/username"
read USERNAME

echo "Password"
read -s PASSWORD

echo "What do you want as a client secret when running your app on localhost against OpenAM?"
read LOCAL_CLIENT_SECRET

REDIRECT_PATH="login/oauth2/code/openam"

# For localhost
curl -k --request POST --data '{"envclass": "t", "agentname": "pensjon-dokdist-localhost"}' https://amag.nais.preprod.local/deleteagent -iv
curl --request POST --data '{"envclass": "t", "agentname": "pensjon-dokdist-localhost", "agentpassword": "'"$LOCAL_CLIENT_SECRET"'", "redirectURLs": ["http://localhost:8080/'$REDIRECT_PATH'"]}' https://amag.nais.preprod.local/createagent -iv

# For environments
curl -k -d '{"application": "pensjon-dokdist", "version": "1", "environment": "q2", "zone": "fss", "username": "'"$USERNAME"'", "password": "'"$PASSWORD"'", "contextroots": ["/'$REDIRECT_PATH'"]}' https://named.nais.preprod.local/configure
curl -k -d '{"application": "pensjon-dokdist", "version": "1", "environment": "p", "zone": "fss", "username": "'"$USERNAME"'", "password": "'"$PASSWORD"'", "contextroots": ["/'$REDIRECT_PATH'"]}' https://named.nais.preprod.local/configure
