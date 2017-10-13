#!/bin/bash

set -euo pipefail

# __date__

# 0.0.2-SNAPSHOT
jar_version=__version__
service_path=__service
mvn_repo_path=__mvnrepo__
artifact=__artifact__
group=__group__

mvn install:install-file \
    -DgroupId="$group" \
    -DartifactId="$artifact" \
    -Dversion="$jar_version" \
    -Dpackaging=jar \
    -Dfile="$service_path"target/"$artifact"-"$jar_version".jar \
    -DpomFile="$service_path"pom.xml \
    -DlocalRepositoryPath=mvn_repo_path \
    -DcreateChecksum=true \
    -Dsources="$service_path"target/"$artifact"-"$jar_version"-sources.jar \
    -Djavadoc="$service_path"target/"$artifact"-"$jar_version"-javadoc.jar

unset jar_version
unset service_path
unset mvn_repo_path
unset artifact
unset group
