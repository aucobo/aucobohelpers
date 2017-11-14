#!/bin/bash

set -euo pipefail

service="aucoborabbitmqhelpers"
version="0.0.4-SNAPSHOT"
mvn_repo="mvn-repo"
artifact="rabbitmq-admin"
group="com.aucobo.helpers.rabbitmq"

today=$(date)
project="aucobo"

default_path=/home/norman/
service_path="$default_path""$project"/"$service"/
deploy_path="$default_path""$project"/"$mvn_repo"/

# TODO: get templates from github via wget...
files=(
    "deploy.sh"
)

for f in ${files[@]}
do
  echo "$f"
  # replacement
  if [ -f "$f" ]; then
    sed  -i "s|\_\_date\_\_|$today|" "$f"
    sed  -i "s|\_\_service\_\_|$service_path|" "$f"
    sed  -i "s|\_\_version\_\_|$version|" "$f"
    sed  -i "s|\_\_mvnrepo\_\_|$deploy_path|" "$f"
    sed  -i "s|\_\_artifact\_\_|$artifact|" "$f"
    sed  -i "s|\_\_group\_\_|$group|" "$f"
  else
      echo "  ..does not exist"
  fi
done

unset service
unset version
unset mvn_repo
unset artifact
unset group
unset today
unset project
unset service_path
unset default_path
unset deploy_path
unset files
