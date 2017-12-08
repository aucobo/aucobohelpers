#!/bin/bash

set -euo pipefail
echo "compiling/building the jar"
mvn clean package

version=0.0.7-SNAPSHOT

mvn install:install-file \
    -DgroupId=com.aucobo.helpers.rabbitmq \
    -DartifactId=rabbitmq-admin \
    -Dversion=$version \
    -Dpackaging=jar \
    -Dfile=/home/norman/aucobo/aucobohelpers/rabbitmq/target/rabbitmq-admin-$version.jar \
    -DpomFile=/home/norman/aucobo/aucobohelpers/rabbitmq/pom.xml \
    -DlocalRepositoryPath=/home/norman/aucobo/mvn-repo \
    -DcreateChecksum=true \
    -Dsources=/home/norman/aucobo/aucobohelpers/rabbitmq/target/rabbitmq-admin-$version-sources.jar \
    -Djavadoc=/home/norman/aucobo/aucobohelpers/rabbitmq/target/rabbitmq-admin-$version-javadoc.jar

unset version
