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

# Usage: generate-e2e-sql-matrix.sh '<json-with-all-18-filter-labels>'
# Output: writes matrix=<JSON>, has-jobs=<true|false>, and need-proxy-image=<true|false> to $GITHUB_OUTPUT

set -euo pipefail

FILTERS_JSON="$1"

get_filter() {
  echo "$FILTERS_JSON" | jq -r ".$1"
}

# Read all 18 filter labels
adapter_proxy=$(get_filter adapter_proxy)
adapter_jdbc=$(get_filter adapter_jdbc)
mode_standalone=$(get_filter mode_standalone)
mode_cluster=$(get_filter mode_cluster)
mode_core=$(get_filter mode_core)
database_mysql=$(get_filter database_mysql)
database_postgresql=$(get_filter database_postgresql)
feature_sharding=$(get_filter feature_sharding)
feature_encrypt=$(get_filter feature_encrypt)
feature_readwrite_splitting=$(get_filter feature_readwrite_splitting)
feature_shadow=$(get_filter feature_shadow)
feature_mask=$(get_filter feature_mask)
feature_broadcast=$(get_filter feature_broadcast)
feature_distsql=$(get_filter feature_distsql)
feature_sql_federation=$(get_filter feature_sql_federation)
core_infra=$(get_filter core_infra)
test_framework=$(get_filter test_framework)
pom_changes=$(get_filter pom_changes)

ALL_SCENARIOS=$(jq -cn '[
  "empty_rules", "distsql_rdl", "passthrough",
  "db", "tbl", "encrypt", "readwrite_splitting",
  "shadow", "mask",
  "dbtbl_with_readwrite_splitting",
  "dbtbl_with_readwrite_splitting_and_encrypt",
  "sharding_and_encrypt", "encrypt_and_readwrite_splitting",
  "encrypt_shadow", "readwrite_splitting_and_shadow",
  "sharding_and_shadow", "sharding_encrypt_shadow",
  "mask_encrypt", "mask_sharding", "mask_encrypt_sharding",
  "db_tbl_sql_federation"
]')

ALL_ADAPTERS='["proxy","jdbc"]'
ALL_MODES='["Standalone","Cluster"]'
ALL_DATABASES='["MySQL","PostgreSQL"]'
SMOKE_SCENARIOS='["empty_rules","db","tbl","encrypt","readwrite_splitting","passthrough"]'

# Build matrix JSON from dimension arrays and scenarios, applying exclude/include rules
build_matrix() {
  local adapters="$1"
  local modes="$2"
  local databases="$3"
  local scenarios="$4"

  jq -cn \
    --argjson adapters "$adapters" \
    --argjson modes "$modes" \
    --argjson databases "$databases" \
    --argjson scenarios "$scenarios" \
    '
    def should_exclude(adapter; mode; scenario):
      (adapter == "jdbc" and scenario == "passthrough") or
      (adapter == "jdbc" and mode == "Cluster") or
      (adapter == "proxy" and mode == "Standalone" and
        (scenario == "empty_rules" or scenario == "distsql_rdl" or scenario == "passthrough"));

    [
      $adapters[] as $adapter |
      $modes[] as $mode |
      $databases[] as $database |
      $scenarios[] as $scenario |
      select(should_exclude($adapter; $mode; $scenario) | not) |
      {adapter: $adapter, mode: $mode, database: $database, scenario: $scenario, "additional-options": ""}
    ] as $base_jobs |

    ([$base_jobs[] | select(.adapter == "proxy" and .mode == "Cluster")] | length > 0) as $has_proxy_cluster |
    ($scenarios | map(select(. == "passthrough")) | length > 0) as $has_passthrough |

    (if $has_proxy_cluster and $has_passthrough
     then [{adapter:"proxy", mode:"Cluster", database:"MySQL", scenario:"passthrough", "additional-options":"-Dmysql-connector-java.version=8.3.0"}]
     else []
     end) as $extra_job |

    {include: ($base_jobs + $extra_job)}
    '
}

# Full fallback: run the entire matrix
if [ "$core_infra" = "true" ] || [ "$test_framework" = "true" ] || [ "$pom_changes" = "true" ]; then
  MATRIX=$(build_matrix "$ALL_ADAPTERS" "$ALL_MODES" "$ALL_DATABASES" "$ALL_SCENARIOS")
  echo "matrix=$(echo "$MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
  echo "has-jobs=true" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=true" >> "$GITHUB_OUTPUT"
  exit 0
fi

# Check whether any relevant dimension changed at all
any_relevant_change=false
if [ "$feature_sharding" = "true" ] || [ "$feature_encrypt" = "true" ] || \
   [ "$feature_readwrite_splitting" = "true" ] || [ "$feature_shadow" = "true" ] || \
   [ "$feature_mask" = "true" ] || [ "$feature_broadcast" = "true" ] || \
   [ "$feature_distsql" = "true" ] || [ "$feature_sql_federation" = "true" ] || \
   [ "$mode_standalone" = "true" ] || [ "$mode_cluster" = "true" ] || [ "$mode_core" = "true" ] || \
   [ "$database_mysql" = "true" ] || [ "$database_postgresql" = "true" ] || \
   [ "$adapter_proxy" = "true" ] || [ "$adapter_jdbc" = "true" ]; then
  any_relevant_change=true
fi

if [ "$any_relevant_change" = "false" ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  exit 0
fi

# Determine adapters
if [ "$adapter_proxy" = "true" ] && [ "$adapter_jdbc" = "false" ]; then
  adapters='["proxy"]'
elif [ "$adapter_jdbc" = "true" ] && [ "$adapter_proxy" = "false" ]; then
  adapters='["jdbc"]'
else
  adapters="$ALL_ADAPTERS"
fi

# Determine modes
if [ "$mode_standalone" = "true" ] && [ "$mode_cluster" = "false" ] && [ "$mode_core" = "false" ]; then
  modes='["Standalone"]'
elif [ "$mode_cluster" = "true" ] && [ "$mode_standalone" = "false" ] && [ "$mode_core" = "false" ]; then
  modes='["Cluster"]'
else
  modes="$ALL_MODES"
fi

# Determine databases
if [ "$database_mysql" = "true" ] && [ "$database_postgresql" = "false" ]; then
  databases='["MySQL"]'
elif [ "$database_postgresql" = "true" ] && [ "$database_mysql" = "false" ]; then
  databases='["PostgreSQL"]'
else
  databases="$ALL_DATABASES"
fi

# Determine scenarios from feature labels
any_feature_triggered=false
scenarios_set=()

add_scenario() {
  local s="$1"
  for existing in "${scenarios_set[@]+"${scenarios_set[@]}"}"; do
    [ "$existing" = "$s" ] && return
  done
  scenarios_set+=("$s")
}

if [ "$feature_sharding" = "true" ]; then
  any_feature_triggered=true
  for s in db tbl dbtbl_with_readwrite_splitting dbtbl_with_readwrite_splitting_and_encrypt \
            sharding_and_encrypt sharding_and_shadow sharding_encrypt_shadow \
            mask_sharding mask_encrypt_sharding db_tbl_sql_federation; do
    add_scenario "$s"
  done
fi

if [ "$feature_encrypt" = "true" ]; then
  any_feature_triggered=true
  for s in encrypt dbtbl_with_readwrite_splitting_and_encrypt sharding_and_encrypt \
            encrypt_and_readwrite_splitting encrypt_shadow sharding_encrypt_shadow \
            mask_encrypt mask_encrypt_sharding; do
    add_scenario "$s"
  done
fi

if [ "$feature_readwrite_splitting" = "true" ]; then
  any_feature_triggered=true
  for s in readwrite_splitting dbtbl_with_readwrite_splitting \
            dbtbl_with_readwrite_splitting_and_encrypt encrypt_and_readwrite_splitting \
            readwrite_splitting_and_shadow; do
    add_scenario "$s"
  done
fi

if [ "$feature_shadow" = "true" ]; then
  any_feature_triggered=true
  for s in shadow encrypt_shadow readwrite_splitting_and_shadow sharding_and_shadow \
            sharding_encrypt_shadow; do
    add_scenario "$s"
  done
fi

if [ "$feature_mask" = "true" ]; then
  any_feature_triggered=true
  for s in mask mask_encrypt mask_sharding mask_encrypt_sharding; do
    add_scenario "$s"
  done
fi

if [ "$feature_distsql" = "true" ]; then
  any_feature_triggered=true
  add_scenario "distsql_rdl"
fi

if [ "$feature_sql_federation" = "true" ]; then
  any_feature_triggered=true
  add_scenario "db_tbl_sql_federation"
fi

if [ "$feature_broadcast" = "true" ]; then
  any_feature_triggered=true
  add_scenario "empty_rules"
fi

# If no feature triggered, use core smoke scenario set
if [ "$any_feature_triggered" = "false" ]; then
  scenarios_json="$SMOKE_SCENARIOS"
else
  scenarios_json=$(printf '%s\n' "${scenarios_set[@]}" | jq -R . | jq -sc .)
fi

MATRIX=$(build_matrix "$adapters" "$modes" "$databases" "$scenarios_json")

JOB_COUNT=$(echo "$MATRIX" | jq '.include | length')

if [ "$JOB_COUNT" -eq 0 ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  exit 0
fi

HAS_PROXY=$(echo "$MATRIX" | jq '[.include[] | select(.adapter == "proxy")] | length > 0')

echo "matrix=$(echo "$MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
echo "has-jobs=true" >> "$GITHUB_OUTPUT"
echo "need-proxy-image=$HAS_PROXY" >> "$GITHUB_OUTPUT"
