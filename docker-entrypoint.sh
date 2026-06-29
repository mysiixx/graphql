#!/bin/sh
set -eu

mkdir -p /app/keys /app/uploads/avatars

if [ ! -f /app/keys/private.key ]; then
  openssl genpkey -algorithm RSA -out /app/keys/private.key -pkeyopt rsa_keygen_bits:2048
  openssl rsa -pubout -in /app/keys/private.key -out /app/keys/public.key
fi

exec java -jar /app/app.jar