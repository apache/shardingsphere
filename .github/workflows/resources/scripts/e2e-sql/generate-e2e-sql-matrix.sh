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
# Output: writes smoke-matrix=<JSON>, full-matrix=<JSON>, matrix=<JSON>(alias for full), has-jobs=<true|false>, and need-proxy-image=<true|false> to $GITHUB_OUTPUT

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
SMOKE_SCENARIOS='["db","tbl"]'

# Build matrix JSON from dimension arrays and scenarios, applying exclude/include rules
build_matrix() {
  local adapters="$1"
  local modes="$2"
  local databases="$3"
  local scenarios="$4"
  local include_extra_job="${5:-true}"

  jq -cn \
    --argjson adapters "$adapters" \
    --argjson modes "$modes" \
    --argjson databases "$databases" \
    --argjson scenarios "$scenarios" \
    --argjson include_extra_job "$include_extra_job" \
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

    (if $include_extra_job and $has_proxy_cluster and $has_passthrough
     then [{adapter:"proxy", mode:"Cluster", database:"MySQL", scenario:"passthrough", "additional-options":"-Dmysql-connector-java.version=8.3.0"}]
     else []
     end) as $extra_job |

    {include: ($base_jobs + $extra_job)}
    '
}

log_counts() {
  local name="$1"
  local matrix_json="$2"
  local count
  count=$(echo "$matrix_json" | jq '.include | length')
  echo "::notice::$name count=$count"
}

any_base_change=false
if [ "$core_infra" = "true" ] || [ "$test_framework" = "true" ] || [ "$pom_changes" = "true" ]; then
  any_base_change=true
  echo "::notice::Base filters triggered (core_infra=$core_infra, test_framework=$test_framework, pom_changes=$pom_changes)"
fi

# Handle workflow_dispatch: run full tests when manually triggered
if [ "${GITHUB_EVENT_NAME:-}" = "workflow_dispatch" ]; then
  any_base_change=true
  echo "::notice::workflow_dispatch detected, enabling full test matrix"
fi

# Check whether any relevant dimension changed at all
any_relevant_change=false
if [ "$feature_sharding" = "true" ] || [ "$feature_encrypt" = "true" ] || \
   [ "$feature_readwrite_splitting" = "true" ] || [ "$feature_shadow" = "true" ] || \
   [ "$feature_mask" = "true" ] || [ "$feature_broadcast" = "true" ] || \
   [ "$feature_distsql" = "true" ] || [ "$feature_sql_federation" = "true" ] || \
   [ "$mode_standalone" = "true" ] || [ "$mode_cluster" = "true" ] || [ "$mode_core" = "true" ] || \
   [ "$database_mysql" = "true" ] || [ "$database_postgresql" = "true" ] || \
   [ "$adapter_proxy" = "true" ] || [ "$adapter_jdbc" = "true" ] || \
   [ "$any_base_change" = "true" ]; then
  any_relevant_change=true
  echo "::notice::At least one relevant filter is true, will generate jobs based on dimensions and scenarios"
fi

if [ "$any_relevant_change" = "false" ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  echo "smoke-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "full-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "::notice::No relevant filters triggered, skipping job generation"
  exit 0
fi

# Determine adapters
if [ "$any_base_change" = "true" ]; then
  adapters="$ALL_ADAPTERS"
  echo "::notice::Base change detected, including all adapters"
elif [ "$adapter_proxy" = "true" ] && [ "$adapter_jdbc" = "false" ]; then
  adapters='["proxy"]'
elif [ "$adapter_jdbc" = "true" ] && [ "$adapter_proxy" = "false" ]; then
  adapters='["jdbc"]'
else
  adapters="$ALL_ADAPTERS"
fi

# Determine modes
if [ "$any_base_change" = "true" ]; then
  modes="$ALL_MODES"
  echo "::notice::Base change detected, including all modes"
elif [ "$mode_standalone" = "true" ] && [ "$mode_cluster" = "false" ] && [ "$mode_core" = "false" ]; then
  modes='["Standalone"]'
elif [ "$mode_cluster" = "true" ] && [ "$mode_standalone" = "false" ] && [ "$mode_core" = "false" ]; then
  modes='["Cluster"]'
else
  modes="$ALL_MODES"
fi

# Determine databases
if [ "$any_base_change" = "true" ]; then
  databases="$ALL_DATABASES"
  echo "::notice::Base change detected, including all databases"
elif [ "$database_mysql" = "true" ] && [ "$database_postgresql" = "false" ]; then
  databases='["MySQL"]'
elif [ "$database_postgresql" = "true" ] && [ "$database_mysql" = "false" ]; then
  databases='["PostgreSQL"]'
else
  databases="$ALL_DATABASES"
fi

# Determine scenarios from feature labels
any_feature_triggered=false
scenarios_set=()
smoke_scenarios_set=()

add_scenario() {
  local s="$1"
  for existing in "${scenarios_set[@]+"${scenarios_set[@]}"}"; do
    [ "$existing" = "$s" ] && return
  done
  scenarios_set+=("$s")
}

add_smoke_scenario() {
  local s="$1"
  for existing in "${smoke_scenarios_set[@]+"${smoke_scenarios_set[@]}"}"; do
    [ "$existing" = "$s" ] && return
  done
  smoke_scenarios_set+=("$s")
}

if [ "$feature_sharding" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "db"
  add_smoke_scenario "tbl"
  for s in db tbl dbtbl_with_readwrite_splitting dbtbl_with_readwrite_splitting_and_encrypt \
            sharding_and_encrypt sharding_and_shadow sharding_encrypt_shadow \
            mask_sharding mask_encrypt_sharding db_tbl_sql_federation; do
    add_scenario "$s"
  done
fi

if [ "$feature_encrypt" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "encrypt"
  for s in encrypt dbtbl_with_readwrite_splitting_and_encrypt sharding_and_encrypt \
            encrypt_and_readwrite_splitting encrypt_shadow sharding_encrypt_shadow \
            mask_encrypt mask_encrypt_sharding; do
    add_scenario "$s"
  done
fi

if [ "$feature_readwrite_splitting" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "readwrite_splitting"
  for s in readwrite_splitting dbtbl_with_readwrite_splitting \
            dbtbl_with_readwrite_splitting_and_encrypt encrypt_and_readwrite_splitting \
            readwrite_splitting_and_shadow; do
    add_scenario "$s"
  done
fi

if [ "$feature_shadow" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "shadow"
  for s in shadow encrypt_shadow readwrite_splitting_and_shadow sharding_and_shadow \
            sharding_encrypt_shadow; do
    add_scenario "$s"
  done
fi

if [ "$feature_mask" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "mask"
  for s in mask mask_encrypt mask_sharding mask_encrypt_sharding; do
    add_scenario "$s"
  done
fi

if [ "$feature_distsql" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "distsql_rdl"
  add_scenario "distsql_rdl"
fi

if [ "$feature_sql_federation" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "db_tbl_sql_federation"
  add_scenario "db_tbl_sql_federation"
fi

if [ "$feature_broadcast" = "true" ]; then
  any_feature_triggered=true
  add_smoke_scenario "empty_rules"
  add_scenario "empty_rules"
fi

if [ "$any_feature_triggered" = "true" ]; then
  adapters=$ALL_ADAPTERS
  modes=$ALL_MODES
  databases=$ALL_DATABASES
  echo "::notice::Feature filters triggered, including all adapters, modes, and databases"
fi

echo "::notice::any_base_change=$any_base_change, any_feature_triggered=$any_feature_triggered, dimensions adapters=$adapters modes=$modes databases=$databases"

# Generate smoke-matrix from default or feature-mapped smoke scenarios, and DO NOT add the extra passthrough job
if [ "$any_feature_triggered" = "true" ]; then
  if [ "${#smoke_scenarios_set[@]}" -eq 0 ]; then
    smoke_scenarios_json="$SMOKE_SCENARIOS"
    echo "::notice::smoke-matrix reason=feature-triggered-without-smoke-mapping, fallback scenarios: $smoke_scenarios_json"
  else
    smoke_scenarios_json=$(printf '%s\n' "${smoke_scenarios_set[@]}" | jq -R . | jq -sc .)
    echo "::notice::smoke-matrix reason=feature-triggered, scenarios: $smoke_scenarios_json"
  fi
else
  smoke_scenarios_json="$SMOKE_SCENARIOS"
  echo "::notice::smoke-matrix reason=default, scenarios: $smoke_scenarios_json"
fi
SMOKE_MATRIX=$(build_matrix "$adapters" "$modes" "$databases" "$smoke_scenarios_json" false)
log_counts "smoke-matrix" "$SMOKE_MATRIX"

# Build full-matrix scenarios
if [ "$any_base_change" = "true" ]; then
  full_scenarios_json="$ALL_SCENARIOS"
  echo "::notice::full-matrix reason=base-change, use all scenarios: $full_scenarios_json"
else
  if [ "$any_feature_triggered" = "false" ]; then
    # When no feature triggered, full-matrix is limited to smoke scenario set
    full_scenarios_json="$SMOKE_SCENARIOS"
    echo "::notice::full-matrix reason=no-feature-triggered, use smoke scenarios: $full_scenarios_json"
  else
    full_scenarios_json=$(printf '%s\n' "${scenarios_set[@]}" | jq -R . | jq -sc .)
    echo "::notice::full-matrix reason=feature-triggered, scenarios: $full_scenarios_json"
  fi
fi

FULL_MATRIX=$(build_matrix "$adapters" "$modes" "$databases" "$full_scenarios_json" true)
log_counts "full-matrix" "$FULL_MATRIX"

# Determine whether there are any jobs at all (based on full-matrix)
FULL_JOB_COUNT=$(echo "$FULL_MATRIX" | jq '.include | length')
if [ "$FULL_JOB_COUNT" -eq 0 ]; then
  echo "smoke-matrix=$(echo "$SMOKE_MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
  echo "full-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  echo "::notice::No jobs generated after applying all filters and rules, skipping job execution"
  exit 0
fi

HAS_PROXY_SMOKE=$(echo "$SMOKE_MATRIX" | jq '[.include[] | select(.adapter == "proxy")] | length > 0')
HAS_PROXY_FULL=$(echo "$FULL_MATRIX" | jq '[.include[] | select(.adapter == "proxy")] | length > 0')
NEED_PROXY_IMAGE=$(jq -cn --argjson a "$HAS_PROXY_SMOKE" --argjson b "$HAS_PROXY_FULL" '$a or $b')

echo "smoke-matrix=$(echo "$SMOKE_MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
echo "full-matrix=$(echo "$FULL_MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
echo "matrix=$(echo "$FULL_MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
echo "has-jobs=true" >> "$GITHUB_OUTPUT"
echo "need-proxy-image=$NEED_PROXY_IMAGE" >> "$GITHUB_OUTPUT"
echo "::notice::Generated $(echo "$SMOKE_MATRIX" | jq '.include | length') smoke jobs and $(echo "$FULL_MATRIX" | jq '.include | length') full jobs. Proxy image needed: $NEED_PROXY_IMAGE"

exit 0
