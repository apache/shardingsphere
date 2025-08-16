#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

BUILD_NEW=false
CONFIG_PATH=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --new)
            BUILD_NEW=true
            shift
            ;;
        --PATH=*)
            CONFIG_PATH="${1#*=}"
            shift
            ;;
        *)
            echo "Usage: sh run_proxy_container.sh --PATH=<path> [--new]"
            exit 1
            ;;
    esac
done

if [ -z "$CONFIG_PATH" ]; then
    echo "Usage: sh run_proxy_container.sh --PATH=<path> [--new]"
    exit 1
fi

if [[ "$CONFIG_PATH" =~ ^[A-Za-z]:\\ ]]; then
    CONFIG_PATH=$(echo "$CONFIG_PATH" | sed 's/\\/\//g')
fi

echo "Normalized Config Path: ${CONFIG_PATH}"

# Define project directory
PROJECT_DIR="distribution/proxy"

# Navigate to the project directory
cd "$PROJECT_DIR" || { echo "Failed to navigate to $PROJECT_DIR"; exit 1; }

IMAGE_NAME="apache/shardingsphere-proxy"

# Extract version using Maven help plugin
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# Check if version extraction was successful
if [ -z "$VERSION" ]; then
    echo "Error: Failed to extract version from Maven project."
    exit 1
fi

echo "Starting Maven build process..."

# If --new flag is set, build a new image
if [ "$BUILD_NEW" = true ]; then
    echo "Building new image..."
    if mvn clean package -P release,docker; then
        echo "Build completed successfully!"
    else
        echo "Error: Build failed!"
        exit 1
    fi
else
    # Check if image exists
    if docker image inspect "${IMAGE_NAME}:${VERSION}" >/dev/null 2>&1; then
        echo "Using existing image: ${IMAGE_NAME}:${VERSION}"
    else
        echo "Image not found. Building new image..."
        if mvn clean package -P release,docker; then
            echo "Build completed successfully!"
        else
            echo "Error: Build failed!"
            exit 1
        fi
    fi
fi

CONTAINER_NAME="shardingsphere-proxy"
if docker ps -a | grep -q ${CONTAINER_NAME}; then
    echo "Found existing container: ${CONTAINER_NAME}"
    read -r -p "Do you want to stop and remove the existing container? (y/n): " response
    case "$response" in
        [yY]|[yY][eE][sS])
            echo "Stopping and removing existing container..."
            docker stop ${CONTAINER_NAME}
            docker rm ${CONTAINER_NAME}
            ;;
        *)
            echo "Operation cancelled by user"
            exit 0
            ;;
    esac
fi

open_sql() {

    case "$(uname)" in
        Linux*)
            if command -v gnome-terminal >/dev/null 2>&1; then
                gnome-terminal -- bash -c
            elif command -v konsole >/dev/null 2>&1; then
                konsole -e bash -c
            else
                echo "No supported terminal found. Open a new terminal and run >> psql -U <user> -h localhost -p 13308 "
            fi
            ;;
        CYGWIN*|MINGW*|MSYS*)
            start cmd /k "psql -U postgres -h localhost -p 13308"
            ;;
        *)
            echo "No supported terminal found. Open a new terminal and run >> psql -U <user> -h localhost -p 13308 "
            ;;
    esac
}

if command -v docker >/dev/null 2>&1; then
    echo -e "\nStarting container with image: apache/shardingsphere-proxy:${VERSION}"
    docker run -d \
                 --name ${CONTAINER_NAME} \
                 -v "${CONFIG_PATH}/conf:/opt/shardingsphere-proxy/conf" \
                 -v "${CONFIG_PATH}/ext-lib:/opt/shardingsphere-proxy/ext-lib" \
                 -e PORT=3308 -p13308:3308 \
                 "apache/shardingsphere-proxy:${VERSION}"
    if docker ps -q -f name=${CONTAINER_NAME}; then
        echo -e "\nContainer started successfully with image: apache/shardingsphere-proxy:${VERSION}"
        echo "Port: 3308"
        # Call the function to open SQL
        open_sql
        docker logs -f ${CONTAINER_NAME}
    else
        echo "Error: Failed to start the container"
        exit 1
    fi
else
    echo "Error: Docker is not installed or not accessible"
    exit 1
fi
