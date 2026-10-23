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

# Unit tests for generate-e2e-sql-matrix.sh
# Usage: bash test-generate-e2e-sql-matrix.sh
# Prerequisites: bash, jq

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_SCRIPT="$SCRIPT_DIR/generate-e2e-sql-matrix.sh"

PASS_COUNT=0
FAIL_COUNT=0

# ---------- helpers ----------

build_filters() {
  local json='{"adapter_proxy":"false","adapter_jdbc":"false","mode_standalone":"false","mode_cluster":"false","mode_core":"false","database_mysql":"false","database_postgresql":"false","feature_sharding":"false","feature_encrypt":"false","feature_readwrite_splitting":"false","feature_shadow":"false","feature_mask":"false","feature_broadcast":"false","feature_distsql":"false","feature_sql_federation":"false","core_infra":"false","test_framework":"false","pom_changes":"false"}'
  for kv in "$@"; do
    local key="${kv%%=*}"
    local val="${kv#*=}"
    json=$(echo "$json" | jq --arg k "$key" --arg v "$val" '.[$k] = $v')
  done
  echo "$json"
}

run_script() {
  local filters="$1"
  local event_name="${2:-pull_request}"
  local target_matrix_algorithm_input="${3:-auto}"
  local output_file
  output_file=$(mktemp)
  GITHUB_OUTPUT="$output_file" GITHUB_EVENT_NAME="$event_name" TARGET_MATRIX_ALGORITHM_INPUT="$target_matrix_algorithm_input" \
    bash "$TARGET_SCRIPT" "$filters" > /dev/null 2>&1
  local rc=$?
  cat "$output_file"
  rm -f "$output_file"
  return $rc
}

get_output() {
  local outputs="$1" key="$2"
  echo "$outputs" | grep "^${key}=" | head -1 | cut -d= -f2-
}

pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  echo "  PASS: $1"
}

fail() {
  FAIL_COUNT=$((FAIL_COUNT + 1))
  echo "  FAIL: $1"
  [[ -n "${2:-}" ]] && echo "        $2"
}

assert_eq() {
  local desc="$1" expected="$2" actual="$3"
  if [[ "$expected" = "$actual" ]]; then
    pass "$desc"
  else
    fail "$desc" "expected='$expected', actual='$actual'"
  fi
}

assert_not_empty() {
  local desc="$1" actual="$2"
  if [[ -n "$actual" ]]; then
    pass "$desc"
  else
    fail "$desc" "expected non-empty value"
  fi
}

assert_all_scenarios() {
  local desc="$1" matrix_json="$2"
  local expected actual
  expected='["db","db_tbl_sql_federation","dbtbl_with_readwrite_splitting","dbtbl_with_readwrite_splitting_and_encrypt","distsql_rdl","distsql_rdl_empty","empty_rules","encrypt","encrypt_and_readwrite_splitting","encrypt_shadow","mask","mask_encrypt","mask_encrypt_sharding","mask_sharding","passthrough","readwrite_splitting","readwrite_splitting_and_shadow","shadow","sharding_and_encrypt","sharding_and_shadow","sharding_encrypt_shadow","tbl"]'
  actual=$(echo "$matrix_json" | jq -c '[.include[].scenario] | unique | sort')
  assert_eq "$desc" "$expected" "$actual"
}

assert_scenarios() {
  local desc="$1" matrix_json="$2" expected_json="$3"
  local expected actual
  expected=$(echo "$expected_json" | jq -c 'sort')
  actual=$(echo "$matrix_json" | jq -c '[.include[].scenario] | unique | sort')
  assert_eq "$desc" "$expected" "$actual"
}

assert_opengauss_scope() {
  local desc="$1" matrix_json="$2"
  local count
  count=$(echo "$matrix_json" | jq '[.include[] | select(
    (.database == "openGauss" and
      (.mode != "Cluster" or .scenario != "encrypt" or .["additional-options"] != ""))
  )] | length')
  assert_eq "$desc" "0" "$count"
}

assert_adapter_parity() {
  local desc="$1" matrix_json="$2"
  local count
  count=$(echo "$matrix_json" | jq '[
    .include[] | select(.database != "H2" and .["additional-options"] == "")
  ] | group_by([.mode, .database, .scenario]) |
    map(select([.[].adapter] | sort != ["jdbc", "proxy"])) | length')
  assert_eq "$desc" "0" "$count"
}

assert_cluster_encrypt_jobs() {
  local desc="$1" matrix_json="$2" expected_json="$3"
  local expected actual
  expected=$(echo "$expected_json" | jq -c 'sort')
  actual=$(echo "$matrix_json" | jq -c '[.include[] | select(
    .mode == "Cluster" and .scenario == "encrypt" and .["additional-options"] == ""
  ) | [.adapter, .database]] | sort')
  assert_eq "$desc" "$expected" "$actual"
}

assert_extra_passthrough_job() {
  local desc="$1" matrix_json="$2"
  local count
  count=$(echo "$matrix_json" | jq '[.include[] | select(
    .adapter == "proxy" and .mode == "Cluster" and .database == "MySQL" and
    .scenario == "passthrough" and .["additional-options"] == "-Dmysql-connector-java.version=8.3.0"
  )] | length')
  assert_eq "$desc" "1" "$count"
}

assert_distsql_bootstrap_job() {
  local desc="$1" matrix_json="$2" expected_count="$3"
  local count
  count=$(echo "$matrix_json" | jq '[.include[] | select(
    .adapter == "jdbc" and .mode == "Standalone" and .database == "H2" and
    .scenario == "distsql_rdl_empty" and .["additional-options"] == ""
  )] | length')
  assert_eq "$desc: bootstrap job count" "$expected_count" "$count"
  assert_eq "$desc: H2 is limited to bootstrap" "$expected_count" "$(echo "$matrix_json" | jq '[.include[] | select(.database == "H2")] | length')"
}

assert_all_field_eq() {
  local desc="$1" matrix_json="$2" field="$3" expected_val="$4"
  local violations
  violations=$(echo "$matrix_json" | jq --arg f "$field" --arg v "$expected_val" \
    '[.include[] | select(.[$f] != $v)] | length')
  assert_eq "$desc" "0" "$violations"
}

# Verify all three dimensions cover the expected range
assert_all_dimensions() {
  local label="$1" matrix_json="$2" expected_database_count="${3:-2}"
  assert_eq "$label: has both adapters" "2" "$(echo "$matrix_json" | jq '[.include[].adapter] | unique | length')"
  assert_eq "$label: has both modes" "2" "$(echo "$matrix_json" | jq '[.include[].mode] | unique | length')"
  assert_eq "$label: has expected database count" "$expected_database_count" "$(echo "$matrix_json" | jq '[.include[].database] | unique | length')"
}

assert_less_than() {
  local desc="$1" actual="$2" upper_bound="$3"
  if [[ "$actual" -lt "$upper_bound" ]]; then
    pass "$desc"
  else
    fail "$desc" "actual='$actual' is not less than upper_bound='$upper_bound'"
  fi
}

assert_matrix_breakdown_consistency() {
  local desc="$1" outputs="$2"
  local smoke_matrix target_matrix overlap remaining_tests total
  local smoke_count target_count expected_remaining_tests expected_total
  smoke_matrix=$(get_output "$outputs" "smoke-matrix")
  target_matrix=$(get_output "$outputs" "target-matrix")
  overlap=$(get_output "$outputs" "target-smoke-overlap-count")
  remaining_tests=$(get_output "$outputs" "estimated-remaining-tests-jobs")
  total=$(get_output "$outputs" "total-two-stage-jobs")
  smoke_count=$(echo "$smoke_matrix" | jq '.include | length')
  target_count=$(echo "$target_matrix" | jq '.include | length')
  expected_remaining_tests=$((target_count - overlap))
  expected_total=$((smoke_count + remaining_tests))
  assert_eq "$desc: remaining-tests=target-overlap" "$expected_remaining_tests" "$remaining_tests"
  assert_eq "$desc: total=smoke+remaining-tests" "$expected_total" "$total"
}

# Common assertions for full-trigger tests (base change / workflow_dispatch)
assert_full_trigger() {
  local label="$1" outputs="$2" expected_algorithm="$3"
  assert_eq "$label: has-jobs" "true" "$(get_output "$outputs" "has-jobs")"
  assert_eq "$label: target-matrix-algorithm" "$expected_algorithm" "$(get_output "$outputs" "target-matrix-algorithm")"
  local smoke_matrix
  smoke_matrix=$(get_output "$outputs" "smoke-matrix")
  assert_scenarios "$label: smoke-matrix uses default scenarios" "$smoke_matrix" '["db","tbl"]'
  local target_matrix
  target_matrix=$(get_output "$outputs" "target-matrix")
  assert_all_scenarios "$label: target-matrix has all 22 scenarios" "$target_matrix"
  assert_distsql_bootstrap_job "$label: target-matrix" "$target_matrix" "1"
  assert_opengauss_scope "$label: openGauss is limited to Cluster encrypt" "$target_matrix"
  assert_extra_passthrough_job "$label: target-matrix has extra passthrough job" "$target_matrix"
  assert_not_empty "$label: target-smoke-overlap-count exists" "$(get_output "$outputs" "target-smoke-overlap-count")"
  assert_not_empty "$label: estimated-remaining-tests-jobs exists" "$(get_output "$outputs" "estimated-remaining-tests-jobs")"
  assert_not_empty "$label: total-two-stage-jobs exists" "$(get_output "$outputs" "total-two-stage-jobs")"
  assert_not_empty "$label: effective-reduction-ratio exists" "$(get_output "$outputs" "effective-reduction-ratio")"
  assert_matrix_breakdown_consistency "$label: matrix-breakdown consistency" "$outputs"
}

# ---------- prerequisite check ----------

if ! command -v jq &> /dev/null; then
  echo "ERROR: jq is required but not installed." >&2
  exit 1
fi

if [[ ! -f "$TARGET_SCRIPT" ]]; then
  echo "ERROR: Target script not found: $TARGET_SCRIPT" >&2
  exit 1
fi

eval "$(sed -n '/^build_matrix() {/,/^}/p' "$TARGET_SCRIPT")"

echo "Running tests for generate-e2e-sql-matrix.sh"
echo ""

# ============================================================
# A. Basic trigger logic
# ============================================================
echo "=== A. Basic trigger logic ==="

echo ""
echo "--- #1: No changes ---"
outputs=$(run_script "$(build_filters)")
assert_eq "#1: has-jobs" "false" "$(get_output "$outputs" "has-jobs")"

echo ""
echo "--- #2: core_infra=true ---"
outputs=$(run_script "$(build_filters core_infra=true)")
assert_full_trigger "#2" "$outputs" "pairwise"
assert_less_than "#2: pairwise target-matrix is reduced" "$(echo "$(get_output "$outputs" "target-matrix")" | jq '.include | length')" "172"

echo ""
echo "--- #3: test_framework=true ---"
outputs=$(run_script "$(build_filters test_framework=true)")
assert_full_trigger "#3" "$outputs" "pairwise"
assert_less_than "#3: pairwise target-matrix is reduced" "$(echo "$(get_output "$outputs" "target-matrix")" | jq '.include | length')" "172"

echo ""
echo "--- #4: pom_changes=true ---"
outputs=$(run_script "$(build_filters pom_changes=true)")
assert_full_trigger "#4" "$outputs" "pairwise"
assert_less_than "#4: pairwise target-matrix is reduced" "$(echo "$(get_output "$outputs" "target-matrix")" | jq '.include | length')" "172"

# ============================================================
# B. workflow_dispatch
# ============================================================
echo ""
echo "=== B. workflow_dispatch ==="

echo ""
echo "--- #5: workflow_dispatch ---"
outputs=$(run_script "$(build_filters)" "workflow_dispatch")
assert_full_trigger "#5" "$outputs" "cartesian"
manual_full=$(get_output "$outputs" "target-matrix")
manual_smoke=$(get_output "$outputs" "smoke-matrix")
expected_cluster_encrypt='[["proxy","MySQL"],["proxy","PostgreSQL"],["proxy","openGauss"],["jdbc","MySQL"],["jdbc","PostgreSQL"],["jdbc","openGauss"]]'
assert_cluster_encrypt_jobs "#5: all six Cluster encrypt jobs occur once" "$manual_full" "$expected_cluster_encrypt"
assert_eq "#5: complete candidate count" "172" "$(echo "$manual_full" | jq '.include | length')"
assert_eq "#5: no duplicate candidates" "0" "$(echo "$manual_full" | jq '.include | length - (unique | length)')"
assert_eq "#5: Smoke covers both adapters in both modes" "16" "$(echo "$manual_smoke" | jq '.include | length')"
assert_adapter_parity "#5: full candidates have matching adapter coverage" "$manual_full"
assert_adapter_parity "#5: Smoke has matching adapter coverage" "$manual_smoke"

echo ""
echo "--- #6: workflow_dispatch with pairwise ---"
outputs=$(run_script "$(build_filters)" "workflow_dispatch" "pairwise")
assert_full_trigger "#6" "$outputs" "pairwise"
assert_less_than "#6: workflow_dispatch pairwise target-matrix is reduced" "$(echo "$(get_output "$outputs" "target-matrix")" | jq '.include | length')" "172"

echo ""
echo "--- #7: workflow_dispatch with invalid algorithm falls back to auto(cartesian) ---"
outputs=$(run_script "$(build_filters)" "workflow_dispatch" "invalid")
assert_full_trigger "#7" "$outputs" "cartesian"

# ============================================================
# C. Dimension reduction
# ============================================================
echo ""
echo "=== C. Dimension reduction ==="

echo ""
echo "--- #8: adapter_proxy only ---"
outputs=$(run_script "$(build_filters adapter_proxy=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#8: smoke all adapter=proxy" "$smoke" "adapter" "proxy"
assert_distsql_bootstrap_job "#8: proxy-only target-matrix" "$(get_output "$outputs" "target-matrix")" "0"

echo ""
echo "--- #9: adapter_jdbc only ---"
outputs=$(run_script "$(build_filters adapter_jdbc=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#9: smoke all adapter=jdbc" "$smoke" "adapter" "jdbc"

echo ""
echo "--- #10: database_mysql only ---"
outputs=$(run_script "$(build_filters database_mysql=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#10: smoke all database=MySQL" "$smoke" "database" "MySQL"

echo ""
echo "--- #11: database_postgresql only ---"
outputs=$(run_script "$(build_filters database_postgresql=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#11: smoke all database=PostgreSQL" "$smoke" "database" "PostgreSQL"

echo ""
echo "--- #12: mode_standalone only ---"
outputs=$(run_script "$(build_filters mode_standalone=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#12: smoke all mode=Standalone" "$smoke" "mode" "Standalone"

echo ""
echo "--- #13: mode_cluster only ---"
outputs=$(run_script "$(build_filters mode_cluster=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_all_field_eq "#13: smoke all mode=Cluster" "$smoke" "mode" "Cluster"
assert_distsql_bootstrap_job "#13: Cluster-only target-matrix" "$(get_output "$outputs" "target-matrix")" "0"

echo ""
echo "--- #14: mode_core only ---"
outputs=$(run_script "$(build_filters mode_core=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_eq "#14: smoke has both modes" "2" "$(echo "$smoke" | jq '[.include[].mode] | unique | length')"

# ============================================================
# D. Feature scenario mapping
# ============================================================
echo ""
echo "=== D. Feature scenario mapping ==="

echo ""
echo "--- #15: feature_sharding ---"
outputs=$(run_script "$(build_filters feature_sharding=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#15: sharding smoke scenarios" "$smoke" '["db","tbl"]'
target=$(get_output "$outputs" "target-matrix")
expected_sharding='["db","tbl","dbtbl_with_readwrite_splitting","dbtbl_with_readwrite_splitting_and_encrypt","sharding_and_encrypt","sharding_and_shadow","sharding_encrypt_shadow","mask_sharding","mask_encrypt_sharding","db_tbl_sql_federation"]'
assert_scenarios "#15: sharding scenarios" "$target" "$expected_sharding"
assert_all_dimensions "#15" "$target"
assert_eq "#15: target-matrix-algorithm" "pairwise" "$(get_output "$outputs" "target-matrix-algorithm")"

echo ""
echo "--- #16: feature_encrypt ---"
outputs=$(run_script "$(build_filters feature_encrypt=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#16: encrypt smoke scenarios" "$smoke" '["encrypt"]'
assert_eq "#16: Smoke includes both adapters without openGauss extras" "8" "$(echo "$smoke" | jq '.include | length')"
target=$(get_output "$outputs" "target-matrix")
expected_encrypt='["encrypt","dbtbl_with_readwrite_splitting_and_encrypt","sharding_and_encrypt","encrypt_and_readwrite_splitting","encrypt_shadow","sharding_encrypt_shadow","mask_encrypt","mask_encrypt_sharding"]'
assert_scenarios "#16: encrypt scenarios" "$target" "$expected_encrypt"
assert_all_dimensions "#16" "$target" "3"
assert_eq "#16: target-matrix-algorithm" "pairwise" "$(get_output "$outputs" "target-matrix-algorithm")"

echo ""
echo "--- #17: feature_readwrite_splitting ---"
outputs=$(run_script "$(build_filters feature_readwrite_splitting=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#17: readwrite_splitting smoke scenarios" "$smoke" '["readwrite_splitting"]'
target=$(get_output "$outputs" "target-matrix")
expected_rws='["readwrite_splitting","dbtbl_with_readwrite_splitting","dbtbl_with_readwrite_splitting_and_encrypt","encrypt_and_readwrite_splitting","readwrite_splitting_and_shadow"]'
assert_scenarios "#17: readwrite_splitting scenarios" "$target" "$expected_rws"
assert_all_dimensions "#17" "$target"

echo ""
echo "--- #18: feature_shadow ---"
outputs=$(run_script "$(build_filters feature_shadow=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#18: shadow smoke scenarios" "$smoke" '["shadow"]'
target=$(get_output "$outputs" "target-matrix")
expected_shadow='["shadow","encrypt_shadow","readwrite_splitting_and_shadow","sharding_and_shadow","sharding_encrypt_shadow"]'
assert_scenarios "#18: shadow scenarios" "$target" "$expected_shadow"
assert_all_dimensions "#18" "$target"

echo ""
echo "--- #19: feature_mask ---"
outputs=$(run_script "$(build_filters feature_mask=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#19: mask smoke scenarios" "$smoke" '["mask"]'
target=$(get_output "$outputs" "target-matrix")
expected_mask='["mask","mask_encrypt","mask_sharding","mask_encrypt_sharding"]'
assert_scenarios "#19: mask scenarios" "$target" "$expected_mask"
assert_all_dimensions "#19" "$target"

echo ""
echo "--- #20: feature_distsql ---"
outputs=$(run_script "$(build_filters feature_distsql=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#20: distsql smoke scenarios" "$smoke" '["distsql_rdl"]'
target=$(get_output "$outputs" "target-matrix")
assert_scenarios "#20: distsql scenarios" "$target" '["distsql_rdl","distsql_rdl_empty"]'
assert_all_dimensions "#20" "$target" "3"
assert_distsql_bootstrap_job "#20: DistSQL target-matrix" "$target" "1"
assert_distsql_bootstrap_job "#20: DistSQL smoke-matrix" "$smoke" "0"

echo ""
echo "--- #21: feature_sql_federation ---"
outputs=$(run_script "$(build_filters feature_sql_federation=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#21: sql_federation smoke scenarios" "$smoke" '["db_tbl_sql_federation"]'
target=$(get_output "$outputs" "target-matrix")
assert_scenarios "#21: sql_federation scenarios" "$target" '["db_tbl_sql_federation"]'
assert_all_dimensions "#21" "$target"

echo ""
echo "--- #22: feature_broadcast ---"
outputs=$(run_script "$(build_filters feature_broadcast=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#22: broadcast smoke scenarios" "$smoke" '["empty_rules"]'
target=$(get_output "$outputs" "target-matrix")
assert_scenarios "#22: broadcast scenarios" "$target" '["empty_rules"]'
assert_all_dimensions "#22" "$target"

# ============================================================
# E. Mixed dimensions
# ============================================================
echo ""
echo "=== E. Mixed dimensions ==="

echo ""
echo "--- #23: base change overrides dimension filters ---"
outputs_mixed=$(run_script "$(build_filters adapter_proxy=true mode_standalone=true database_mysql=true core_infra=true)")
outputs_base=$(run_script "$(build_filters core_infra=true)")
assert_eq "#23: same target-matrix as core_infra only" \
  "$(get_output "$outputs_base" "target-matrix")" \
  "$(get_output "$outputs_mixed" "target-matrix")"
assert_eq "#23: same smoke-matrix as core_infra only" \
  "$(get_output "$outputs_base" "smoke-matrix")" \
  "$(get_output "$outputs_mixed" "smoke-matrix")"

echo ""
echo "--- #24: feature trigger overrides dimension filters ---"
outputs_mixed=$(run_script "$(build_filters adapter_jdbc=true feature_sharding=true)")
outputs_feature=$(run_script "$(build_filters feature_sharding=true)")
assert_eq "#24: same target-matrix as feature_sharding only" \
  "$(get_output "$outputs_feature" "target-matrix")" \
  "$(get_output "$outputs_mixed" "target-matrix")"
assert_eq "#24: same smoke-matrix as feature_sharding only" \
  "$(get_output "$outputs_feature" "smoke-matrix")" \
  "$(get_output "$outputs_mixed" "smoke-matrix")"

echo ""
echo "--- #25: mixed features merge smoke scenarios ---"
outputs=$(run_script "$(build_filters feature_sharding=true feature_encrypt=true)")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_scenarios "#25: smoke scenarios are merged and deduplicated" "$smoke" '["db","encrypt","tbl"]'

echo ""
echo "--- #26: DistSQL bootstrap remains in Remaining Tests ---"
filters=$(build_filters feature_distsql=true)
outputs=$(run_script "$filters" "pull_request" "cartesian")
target=$(get_output "$outputs" "target-matrix")
smoke=$(get_output "$outputs" "smoke-matrix")
assert_distsql_bootstrap_job "#26: cartesian target-matrix" "$target" "1"
output_file=$(mktemp)
GITHUB_OUTPUT="$output_file" bash "$SCRIPT_DIR/generate-remaining-matrix.sh" "$filters" "$target" "$smoke"
remaining=$(get_output "$(cat "$output_file")" "matrix")
rm -f "$output_file"
assert_distsql_bootstrap_job "#26: remaining-matrix" "$remaining" "1"

echo ""
echo "--- #27: Cluster encrypt additions remain in Remaining Tests ---"
output_file=$(mktemp)
GITHUB_OUTPUT="$output_file" bash "$SCRIPT_DIR/generate-remaining-matrix.sh" "$(build_filters)" "$manual_full" "$manual_smoke"
remaining=$(get_output "$(cat "$output_file")" "matrix")
rm -f "$output_file"
assert_eq "#27: Remaining Tests is exactly full minus Smoke" \
  "$(jq -cn --argjson full "$manual_full" --argjson smoke "$manual_smoke" '{include: ($full.include - $smoke.include)}')" "$remaining"

echo ""
echo "--- #28: Cluster encrypt includes follow effective dimensions ---"
matrix=$(build_matrix '["proxy"]' '["Cluster"]' '["PostgreSQL"]' '["encrypt"]')
assert_cluster_encrypt_jobs "#28: proxy respects database selection and adds openGauss" "$matrix" '[["proxy","PostgreSQL"],["proxy","openGauss"]]'
matrix=$(build_matrix '["jdbc"]' '["Cluster"]' '["MySQL"]' '["encrypt"]')
assert_cluster_encrypt_jobs "#28: JDBC respects MySQL selection and adds openGauss" "$matrix" '[["jdbc","MySQL"],["jdbc","openGauss"]]'
matrix=$(build_matrix '["jdbc"]' '["Cluster"]' '["PostgreSQL"]' '["encrypt"]')
assert_cluster_encrypt_jobs "#28: JDBC respects PostgreSQL selection and adds openGauss" "$matrix" '[["jdbc","PostgreSQL"],["jdbc","openGauss"]]'
matrix=$(build_matrix '["proxy","jdbc"]' '["Standalone"]' '["MySQL","PostgreSQL"]' '["encrypt"]')
assert_eq "#28: Standalone encrypt has only four original jobs" "4" "$(echo "$matrix" | jq '.include | length')"

# ============================================================
# Summary
# ============================================================
echo ""
echo "============================================================"
total=$((PASS_COUNT + FAIL_COUNT))
echo "Results: $PASS_COUNT passed, $FAIL_COUNT failed (total $total)"
if [[ "$FAIL_COUNT" -gt 0 ]]; then
  exit 1
fi
echo "All tests passed!"
