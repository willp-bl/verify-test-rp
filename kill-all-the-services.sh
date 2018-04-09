#!/usr/bin/env bash

services=${@:-"test-rp"}

for service in $services; do
  pkill -9 -f "${service}.jar"
done

exit 0
