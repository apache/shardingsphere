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

# Usage: generate-remaining-matrix.sh '<filter-json>' '<full-matrix-json>' '<smoke-matrix-json>'
# Generates the Stage 2 matrix by subtracting smoke combinations from the full matrix.
# Output: writes matrix=<JSON>, has-remaining-jobs=<true|false> to $GITHUB_OUTPUT

set -euo pipefail

FILTER_JSON="$1"
FULL_MATRIX="$2"
SMOKE_MATRIX="$3"

# Parse completed jobs from smoke matrix
COMPLETED_JOBS=$(echo "$SMOKE_MATRIX" | jq -c '.include // []')

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
