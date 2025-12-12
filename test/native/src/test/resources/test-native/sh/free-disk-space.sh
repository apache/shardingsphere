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

# This file is only used in the Bash of ShardingSphere in GitHub Actions environment and should not be executed manually in a development environment.
# Background information can be found at https://github.com/apache/shardingsphere/pull/37333 .
set -e
RMZ_VERSION="3.1.1"
arch="$(uname -m)"
case "${arch}" in
    x86_64|amd64) ASSET="x86_64-unknown-linux-gnu-rmz" ;;
    aarch64|arm64) ASSET="aarch64-unknown-linux-gnu-rmz" ;;
    *) echo "Unsupported arch: ${arch}"; exit 0 ;;
esac
RMZ_RELEASE_URL="https://github.com/SUPERCILEX/fuc/releases/download/${RMZ_VERSION}/${ASSET}"
tmpfile=$(mktemp)
if ! curl -fsSL -o "${tmpfile}" "${RMZ_RELEASE_URL}"; then
    rm -f "${tmpfile}"
    exit 0
fi
sudo install -m 0755 "${tmpfile}" /usr/local/bin/rmz
rm -f "${tmpfile}"
COMMAND_RMZ=$(command -v rmz || true)
if ! [[ -x "${COMMAND_RMZ}" ]]; then
    exit 0
fi
sudo rmz -f /usr/local/lib/android || true
sudo rmz -f /opt/android || true
sudo rmz -f /usr/local/android-sdk || true
sudo rmz -f /home/runner/Android || true
ANDROID_PACKAGES=$(dpkg -l | grep -E "^ii.*(android|adb)" | awk '{print $2}' | tr '\n' ' ' || true)
if [[ -n "${ANDROID_PACKAGES}" ]]; then
    sudo apt-get remove -y "${ANDROID_PACKAGES}" --fix-missing > /dev/null 2>&1 || true
    sudo apt-get autoremove -y > /dev/null 2>&1 || true
    sudo apt-get clean > /dev/null 2>&1 || true
fi
sudo rmz -f /usr/share/dotnet || true
sudo rmz -f /usr/share/doc/dotnet-* || true
DOTNET_PACKAGES=$(dpkg -l | grep -E "^ii.*dotnet" | awk '{print $2}' | tr '\n' ' ' || true)
if [[ -n "${DOTNET_PACKAGES}" ]]; then
    sudo apt-get remove -y "${DOTNET_PACKAGES}" --fix-missing > /dev/null 2>&1 || true
    sudo apt-get autoremove -y > /dev/null 2>&1 || true
    sudo apt-get clean > /dev/null 2>&1 || true
fi
sudo rmz -f /opt/ghc || true
sudo rmz -f /usr/local/.ghcup || true
sudo rmz -f /opt/cabal || true
sudo rmz -f /home/runner/.ghcup || true
sudo rmz -f /home/runner/.cabal || true
HASKELL_PACKAGES=$(dpkg -l | grep -E "^ii.*(ghc|haskell|cabal)" | awk '{print $2}' | tr '\n' ' ' || true)
if [[ -n "${HASKELL_PACKAGES}" ]]; then
    sudo apt-get remove -y "${HASKELL_PACKAGES}" --fix-missing > /dev/null 2>&1 || true
    sudo apt-get autoremove -y > /dev/null 2>&1 || true
    sudo apt-get clean > /dev/null 2>&1 || true
fi
