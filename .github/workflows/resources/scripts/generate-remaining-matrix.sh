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

# Usage: generate-remaining-matrix.sh '<filter-json>' '<previous-matrix-json>'
# Generates the Stage 2 matrix by subtracting Stage 1 combinations from the full matrix.
# Output: writes matrix=<JSON>, has-remaining-jobs=<true|false> to $GITHUB_OUTPUT

set -euo pipefail

FILTER_JSON="$1"
PREVIOUS_MATRIX="$2"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Parse completed jobs from Stage 1 matrix
COMPLETED_JOBS=$(echo "$PREVIOUS_MATRIX" | jq -c '.include // []')

# Generate the full matrix by calling the base script with core_infra=true so all
# scenario/adapter/mode/database combinations (plus exclude/include rules) are produced.
# Setting core_infra=true triggers the full-matrix fallback path in generate-e2e-sql-matrix.sh,
# while resetting test_framework and pom_changes to false avoids double-triggering the same path.
FULL_FILTER_JSON=$(echo "$FILTER_JSON" | jq -c '.core_infra = "true" | .test_framework = "false" | .pom_changes = "false"')

TEMP_OUTPUT=$(mktemp)
GITHUB_OUTPUT="$TEMP_OUTPUT" bash "$SCRIPT_DIR/generate-e2e-sql-matrix.sh" "$FULL_FILTER_JSON"
FULL_MATRIX=$(grep '^matrix=' "$TEMP_OUTPUT" | cut -d= -f2-)
rm -f "$TEMP_OUTPUT"

if [ -z "$FULL_MATRIX" ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-remaining-jobs=false" >> "$GITHUB_OUTPUT"
  exit 0
fi

# Subtract Stage 1 completed jobs from the full matrix
REMAINING_JOBS=$(jq -cn \
  --argjson full "$FULL_MATRIX" \
  --argjson completed "$COMPLETED_JOBS" \
  '[
    $full.include[] as $job |
    select(
      [$completed[] | select(
        .adapter == $job.adapter and
        .mode == $job.mode and
        .database == $job.database and
        .scenario == $job.scenario and
        .["additional-options"] == $job["additional-options"]
      )] | length == 0
    ) |
    $job
  ]')

JOB_COUNT=$(echo "$REMAINING_JOBS" | jq 'length')

if [ "$JOB_COUNT" -eq 0 ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-remaining-jobs=false" >> "$GITHUB_OUTPUT"
  exit 0
fi

echo "matrix=$(echo "{\"include\":$REMAINING_JOBS}" | jq -c .)" >> "$GITHUB_OUTPUT"
echo "has-remaining-jobs=true" >> "$GITHUB_OUTPUT"
