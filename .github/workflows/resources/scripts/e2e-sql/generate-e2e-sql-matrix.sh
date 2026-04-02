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
# Environment:
#   FULL_MATRIX_ALGORITHM_INPUT: auto|cartesian|pairwise (optional, default auto)
# Output: writes smoke-matrix=<JSON>, full-matrix=<JSON>, matrix=<JSON>(alias for full), has-jobs=<true|false>, need-proxy-image=<true|false>,
#         full-matrix-algorithm=<cartesian|pairwise|auto>, full-smoke-overlap-count=<N>, estimated-stage2-jobs=<N>,
#         total-two-stage-jobs=<N>, and effective-reduction-ratio=<0..1> to $GITHUB_OUTPUT

set -euo pipefail

FILTERS_JSON="$1"

# Read all 18 filter labels
IFS=$'\t' read -r \
  adapter_proxy adapter_jdbc mode_standalone mode_cluster mode_core \
  database_mysql database_postgresql \
  feature_sharding feature_encrypt feature_readwrite_splitting feature_shadow feature_mask \
  feature_broadcast feature_distsql feature_sql_federation \
  core_infra test_framework pom_changes <<< "$(echo "$FILTERS_JSON" | jq -r '[
    .adapter_proxy, .adapter_jdbc, .mode_standalone, .mode_cluster, .mode_core,
    .database_mysql, .database_postgresql,
    .feature_sharding, .feature_encrypt, .feature_readwrite_splitting, .feature_shadow, .feature_mask,
    .feature_broadcast, .feature_distsql, .feature_sql_federation,
    .core_infra, .test_framework, .pom_changes
  ] | @tsv')"

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

build_pairwise_matrix() {
  # Algorithm note:
  # This implementation is NOT ACTS/IPOG itself. It is a deterministic greedy
  # 2-way covering strategy with mandatory scenario anchors:
  #   1) Start from constrained candidate jobs (existing include/exclude rules).
  #   2) Preselect additional-options jobs (to preserve required special cases).
  #   3) Select one anchor job per scenario.
  #   4) Greedily add the job that covers the most uncovered parameter pairs.
  #
  # This approach follows public combinatorial-testing principles and does not
  # copy ACTS implementation code.
  # References:
  # - NIST combinatorial testing project (ACTS/IPOG background):
  #   https://csrc.nist.gov/projects/automated-combinatorial-testing-for-software
  # - Interaction rule overview:
  #   https://csrc.nist.gov/projects/automated-combinatorial-testing-for-software/combinatorial-methods-in-testing/interactions-involved-in-software-failures
  # - Greedy set-cover approximation idea (well-known heuristic):
  #   https://en.wikipedia.org/wiki/Set_cover_problem
  local candidate_matrix="$1"
  local scenario_list_json="$2"
  local job_file selected_file
  job_file=$(mktemp)
  selected_file=$(mktemp)
  echo "$candidate_matrix" | jq -r '
    .include[] |
    [
      ([
        ("am:" + .adapter + "|" + .mode),
        ("ad:" + .adapter + "|" + .database),
        ("md:" + .mode + "|" + .database),
        ("as:" + .adapter + "|" + .scenario),
        ("ms:" + .mode + "|" + .scenario),
        ("ds:" + .database + "|" + .scenario)
      ] | join(",")),
      ((if .adapter == "proxy" then 4 else 0 end) + (if .mode == "Cluster" then 2 else 0 end) + (if .database == "MySQL" then 1 else 0 end) + (if .["additional-options"] == "" then 1 else 0 end) | tostring),
      .scenario,
      (if .["additional-options"] != "" then "true" else "false" end),
      (tojson)
    ] | @tsv
  ' > "$job_file"
  declare -a jobs job_scores job_scenarios job_has_additional selected_indexes selected_flags
  declare -a job_pair_1 job_pair_2 job_pair_3 job_pair_4 job_pair_5 job_pair_6
  jobs=()
  job_scores=()
  job_scenarios=()
  job_has_additional=()
  selected_indexes=()
  selected_flags=()
  job_pair_1=()
  job_pair_2=()
  job_pair_3=()
  job_pair_4=()
  job_pair_5=()
  job_pair_6=()
  local pairs score scenario has_additional job_json idx
  local pair1 pair2 pair3 pair4 pair5 pair6
  local best_idx best_score candidate_score candidate_new
  local pick_idx pick_new pick_score required_scenario new_pairs
  local _pair
  local covered_pairs_blob
  covered_pairs_blob=$'\n'
  while IFS=$'\t' read -r pairs score scenario has_additional job_json; do
    IFS=',' read -r pair1 pair2 pair3 pair4 pair5 pair6 <<< "$pairs"
    idx="${#jobs[@]}"
    jobs[$idx]="$job_json"
    job_scores[$idx]="$score"
    job_scenarios[$idx]="$scenario"
    job_has_additional[$idx]="$has_additional"
    selected_flags[$idx]=0
    job_pair_1[$idx]="$pair1"
    job_pair_2[$idx]="$pair2"
    job_pair_3[$idx]="$pair3"
    job_pair_4[$idx]="$pair4"
    job_pair_5[$idx]="$pair5"
    job_pair_6[$idx]="$pair6"
  done < "$job_file"
  if [ "${#jobs[@]}" -eq 0 ]; then
    rm -f "$job_file" "$selected_file"
    echo '{"include":[]}'
    return
  fi
  add_selected_index() {
    local idx="$1"
    local _pair
    [ "${selected_flags[$idx]}" -eq 1 ] && return
    selected_flags[$idx]=1
    selected_indexes+=("$idx")
    for _pair in "${job_pair_1[$idx]}" "${job_pair_2[$idx]}" "${job_pair_3[$idx]}" "${job_pair_4[$idx]}" "${job_pair_5[$idx]}" "${job_pair_6[$idx]}"; do
      case "$covered_pairs_blob" in
        *$'\n'"$_pair"$'\n'*) ;;
        *) covered_pairs_blob+="${_pair}"$'\n' ;;
      esac
    done
  }
  count_new_pairs() {
    local idx="$1"
    new_pairs=0
    local _pair
    for _pair in "${job_pair_1[$idx]}" "${job_pair_2[$idx]}" "${job_pair_3[$idx]}" "${job_pair_4[$idx]}" "${job_pair_5[$idx]}" "${job_pair_6[$idx]}"; do
      case "$covered_pairs_blob" in
        *$'\n'"$_pair"$'\n'*) ;;
        *) new_pairs=$((new_pairs + 1)) ;;
      esac
    done
  }
  for idx in "${!jobs[@]}"; do
    if [ "${job_has_additional[$idx]}" = "true" ]; then
      add_selected_index "$idx"
    fi
  done
  while IFS= read -r required_scenario; do
    best_idx=-1
    best_score=-1
    for idx in "${!jobs[@]}"; do
      [ "${job_scenarios[$idx]}" = "$required_scenario" ] || continue
      candidate_score="${job_scores[$idx]}"
      if [ "$best_idx" -lt 0 ] || [ "$candidate_score" -gt "$best_score" ] || { [ "$candidate_score" -eq "$best_score" ] && [ "$idx" -lt "$best_idx" ]; }; then
        best_idx="$idx"
        best_score="$candidate_score"
      fi
    done
    [ "$best_idx" -lt 0 ] || add_selected_index "$best_idx"
  done < <(echo "$scenario_list_json" | jq -r '.[]')
  while true; do
    pick_idx=-1
    pick_new=0
    pick_score=-1
    for idx in "${!jobs[@]}"; do
      [ "${selected_flags[$idx]}" -eq 1 ] && continue
      count_new_pairs "$idx"
      candidate_new="$new_pairs"
      [ "$candidate_new" -gt 0 ] || continue
      candidate_score="${job_scores[$idx]}"
      if [ "$pick_idx" -lt 0 ] || [ "$candidate_new" -gt "$pick_new" ] || { [ "$candidate_new" -eq "$pick_new" ] && [ "$candidate_score" -gt "$pick_score" ]; } || { [ "$candidate_new" -eq "$pick_new" ] && [ "$candidate_score" -eq "$pick_score" ] && [ "$idx" -lt "$pick_idx" ]; }; then
        pick_idx="$idx"
        pick_new="$candidate_new"
        pick_score="$candidate_score"
      fi
    done
    [ "$pick_idx" -lt 0 ] && break
    add_selected_index "$pick_idx"
  done
  for idx in "${selected_indexes[@]}"; do
    echo "${jobs[$idx]}" >> "$selected_file"
  done
  pairwise_matrix=$(jq -sc '{include: .}' "$selected_file")
  rm -f "$job_file" "$selected_file"
  echo "$pairwise_matrix"
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

manual_dispatch=false
if [ "${GITHUB_EVENT_NAME:-}" = "workflow_dispatch" ]; then
  manual_dispatch=true
  echo "::notice::workflow_dispatch detected"
fi

requested_full_matrix_algorithm="${FULL_MATRIX_ALGORITHM_INPUT:-auto}"
case "$requested_full_matrix_algorithm" in
  auto|cartesian|pairwise)
    ;;
  *)
    echo "::warning::Invalid FULL_MATRIX_ALGORITHM_INPUT=$requested_full_matrix_algorithm, fallback to auto"
    requested_full_matrix_algorithm="auto"
    ;;
esac

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

if [ "$manual_dispatch" = "true" ]; then
  any_relevant_change=true
  echo "::notice::workflow_dispatch enforces relevant-change path"
fi

if [ "$any_relevant_change" = "false" ]; then
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  echo "smoke-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "full-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "full-matrix-algorithm=auto" >> "$GITHUB_OUTPUT"
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

if [ "$manual_dispatch" = "true" ]; then
  adapters=$ALL_ADAPTERS
  modes=$ALL_MODES
  databases=$ALL_DATABASES
  echo "::notice::workflow_dispatch uses all adapters, modes, and databases"
fi

effective_full_matrix_algorithm="$requested_full_matrix_algorithm"
if [ "$effective_full_matrix_algorithm" = "auto" ]; then
  if [ "$manual_dispatch" = "true" ]; then
    effective_full_matrix_algorithm="cartesian"
  elif [ "$any_base_change" = "true" ] || [ "$any_feature_triggered" = "true" ]; then
    effective_full_matrix_algorithm="pairwise"
  else
    effective_full_matrix_algorithm="cartesian"
  fi
fi

echo "::notice::any_base_change=$any_base_change, any_feature_triggered=$any_feature_triggered, dimensions adapters=$adapters modes=$modes databases=$databases, full-matrix-algorithm(requested=$requested_full_matrix_algorithm,effective=$effective_full_matrix_algorithm)"

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
if [ "$manual_dispatch" = "true" ]; then
  full_scenarios_json="$ALL_SCENARIOS"
  echo "::notice::full-matrix reason=workflow-dispatch, use all scenarios: $full_scenarios_json"
elif [ "$any_base_change" = "true" ]; then
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

FULL_MATRIX_CANDIDATE=$(build_matrix "$adapters" "$modes" "$databases" "$full_scenarios_json" true)
FULL_MATRIX_CANDIDATE_COUNT=$(echo "$FULL_MATRIX_CANDIDATE" | jq '.include | length')
if [ "$effective_full_matrix_algorithm" = "pairwise" ]; then
  if [ "$FULL_MATRIX_CANDIDATE_COUNT" -le 20 ]; then
    FULL_MATRIX="$FULL_MATRIX_CANDIDATE"
    echo "::notice::full-matrix pairwise skipped due to small candidate set: candidate=$FULL_MATRIX_CANDIDATE_COUNT"
  else
    FULL_MATRIX=$(build_pairwise_matrix "$FULL_MATRIX_CANDIDATE" "$full_scenarios_json")
    echo "::notice::full-matrix reduction applied: candidate=$FULL_MATRIX_CANDIDATE_COUNT, reduced=$(echo "$FULL_MATRIX" | jq '.include | length')"
  fi
else
  FULL_MATRIX="$FULL_MATRIX_CANDIDATE"
fi
log_counts "full-matrix" "$FULL_MATRIX"

# Determine whether there are any jobs at all (based on full-matrix)
FULL_JOB_COUNT=$(echo "$FULL_MATRIX" | jq '.include | length')
SMOKE_JOB_COUNT=$(echo "$SMOKE_MATRIX" | jq '.include | length')
FULL_SMOKE_OVERLAP_COUNT=$(jq -n --argjson full "$FULL_MATRIX" --argjson smoke "$SMOKE_MATRIX" '
  [
    $full.include[] as $job |
    select(
      [$smoke.include[] | select(
        .adapter == $job.adapter and
        .mode == $job.mode and
        .database == $job.database and
        .scenario == $job.scenario and
        .["additional-options"] == $job["additional-options"]
      )] | length > 0
    )
  ] | length
')
ESTIMATED_STAGE2_JOBS=$((FULL_JOB_COUNT - FULL_SMOKE_OVERLAP_COUNT))
TOTAL_TWO_STAGE_JOBS=$((SMOKE_JOB_COUNT + ESTIMATED_STAGE2_JOBS))
if [ "$FULL_MATRIX_CANDIDATE_COUNT" -eq 0 ]; then
  EFFECTIVE_REDUCTION_RATIO="0"
else
  EFFECTIVE_REDUCTION_RATIO=$(jq -cn --argjson candidate "$FULL_MATRIX_CANDIDATE_COUNT" --argjson total "$TOTAL_TWO_STAGE_JOBS" '1 - ($total / $candidate)')
fi
echo "::notice::matrix-breakdown full=$FULL_JOB_COUNT smoke=$SMOKE_JOB_COUNT overlap=$FULL_SMOKE_OVERLAP_COUNT estimated-stage2=$ESTIMATED_STAGE2_JOBS total-two-stage=$TOTAL_TWO_STAGE_JOBS effective-reduction-ratio=$EFFECTIVE_REDUCTION_RATIO"
if [ "$FULL_JOB_COUNT" -eq 0 ]; then
  echo "smoke-matrix=$(echo "$SMOKE_MATRIX" | jq -c .)" >> "$GITHUB_OUTPUT"
  echo "full-matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "matrix={\"include\":[]}" >> "$GITHUB_OUTPUT"
  echo "has-jobs=false" >> "$GITHUB_OUTPUT"
  echo "need-proxy-image=false" >> "$GITHUB_OUTPUT"
  echo "full-matrix-algorithm=$effective_full_matrix_algorithm" >> "$GITHUB_OUTPUT"
  echo "full-smoke-overlap-count=0" >> "$GITHUB_OUTPUT"
  echo "estimated-stage2-jobs=0" >> "$GITHUB_OUTPUT"
  echo "total-two-stage-jobs=0" >> "$GITHUB_OUTPUT"
  echo "effective-reduction-ratio=0" >> "$GITHUB_OUTPUT"
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
echo "full-matrix-algorithm=$effective_full_matrix_algorithm" >> "$GITHUB_OUTPUT"
echo "full-smoke-overlap-count=$FULL_SMOKE_OVERLAP_COUNT" >> "$GITHUB_OUTPUT"
echo "estimated-stage2-jobs=$ESTIMATED_STAGE2_JOBS" >> "$GITHUB_OUTPUT"
echo "total-two-stage-jobs=$TOTAL_TWO_STAGE_JOBS" >> "$GITHUB_OUTPUT"
echo "effective-reduction-ratio=$EFFECTIVE_REDUCTION_RATIO" >> "$GITHUB_OUTPUT"
echo "::notice::Generated $SMOKE_JOB_COUNT smoke jobs, $FULL_JOB_COUNT full jobs, overlap=$FULL_SMOKE_OVERLAP_COUNT, estimated stage2 jobs=$ESTIMATED_STAGE2_JOBS, total two-stage jobs=$TOTAL_TWO_STAGE_JOBS, effective reduction ratio=$EFFECTIVE_REDUCTION_RATIO. Proxy image needed: $NEED_PROXY_IMAGE"

exit 0
