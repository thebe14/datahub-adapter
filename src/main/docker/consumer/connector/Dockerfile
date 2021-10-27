#
# Copyright 2021 EGI Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# 1.--- Build IDSA connector ---------------------------------------------------
#
FROM maven:3-jdk-11 AS maven

# Get source code
WORKDIR /app

RUN git clone https://github.com/International-Data-Spaces-Association/DataspaceConnector.git .

# Resolve dependencies
RUN mvn -e -B dependency:resolve
RUN mvn -e -B dependency:resolve-plugins

# Build
RUN mvn -e -B clean package -DskipTests -Dmaven.javadoc.skip=true

#
# 2.--- Copy connector, certs, config to final container  ---------------------
#
FROM adoptopenjdk/openjdk11:alpine-slim

RUN apk add bash gettext

# Copy the jar and build image
WORKDIR /app
COPY --from=maven /app/target/*.jar app.jar
COPY conf/* conf/

# Setup entrypoint
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8080
EXPOSE 29292

ENTRYPOINT ["/app/entrypoint.sh"]
