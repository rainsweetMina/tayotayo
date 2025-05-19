#!/bin/bash
until mysqladmin ping -h "localhost" --silent; do
  echo "Waiting for MySQL to be ready..."
  sleep 2
done

exec "$@"

