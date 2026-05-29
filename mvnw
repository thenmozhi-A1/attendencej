#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

# ---- DEFAULT_MAVEN_VERSION ----
DEFAULT_MAVEN_VERSION="3.9.6"

# ---- MAVEN_PROJECTBASEDIR ----
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
export MAVEN_PROJECTBASEDIR

# ---- JAVA_HOME ----
if [ -z "$JAVA_HOME" ]; then
  JAVACMD="java"
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

# ---- MAVEN_HOME ----
find_maven_home() {
  local mavenHome
  if [ -n "$M2_HOME" ]; then
    mavenHome="$M2_HOME"
  elif [ -n "$MAVEN_HOME" ]; then
    mavenHome="$MAVEN_HOME"
  fi
  echo "$mavenHome"
}

MAVEN_HOME=$(find_maven_home)

if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/dists/apache-maven-${DEFAULT_MAVEN_VERSION}"
  if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven ${DEFAULT_MAVEN_VERSION}..."
    mkdir -p "$MAVEN_HOME"
    DOWNLOAD_URL="https://archive.apache.org/dist/maven/maven-3/${DEFAULT_MAVEN_VERSION}/binaries/apache-maven-${DEFAULT_MAVEN_VERSION}-bin.tar.gz"
    curl -fsSL "$DOWNLOAD_URL" | tar -xzf - -C "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/dists/"
    mv "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/dists/apache-maven-${DEFAULT_MAVEN_VERSION}-bin/apache-maven-${DEFAULT_MAVEN_VERSION}" "$MAVEN_HOME" 2>/dev/null || true
  fi
fi

MAVEN_CMD="$MAVEN_HOME/bin/mvn"

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$MAVEN_HOME/boot/plexus-classworlds-*.jar" \
  "-Dclassworlds.conf=$MAVEN_HOME/bin/m2.conf" \
  "-Dmaven.home=$MAVEN_HOME" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  ${MAVEN_CMD} "$@"
