#!/bin/sh

USERNAME=benkenobi
PASSWORD=starwars
USER_EMAILS=ben.kenobi@jedi.tatooine.org
GIVEN_NAME=Obi-Wan
FAMILY_NAME=Kenobi

CLIENTID=myapp
CLIENTSECRET=myappclientsecret
CLIENT_REDIRECT_URI=http://localhost:3030/callback

ADMIN=admin
ADMINPASSWD=adminsecret

# localhost deployed UAA
UAAHOST=http://localhost:8080/uaa

# targeting UAA server
uaac target $UAAHOST

# authenticating with admin
uaac token client get $ADMIN -s $ADMINPASSWD

# creating client
uaac client add $CLIENTID \
  --secret "$CLIENTSECRET" \
  --scope "openid" \
  --authorized_grant_type "authorization_code client_credentials" \
  --redirect_uri "$CLIENT_REDIRECT_URI" \
  --authorities "uaa.resource"
  
# creating user
uaac user add $USERNAME -p $PASSWORD \
  --emails "$USER_EMAILS"
  
# optional info
uaac user update $USERNAME \
  --given_name "$GIVEN_NAME" \
  --family_name "$FAMILY_NAME"

# retrieving client info
uaac client get $CLIENTID

# retrieving user info
uaac user get $USERNAME

