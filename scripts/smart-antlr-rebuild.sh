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

# -------------------------------------------------------------------------
# Description: Smartly identify and recompile ShardingSphere ANTLR4 modules.
# It uses 'mvn compile' with skips to ensure 'import' grammars are resolved.
# -------------------------------------------------------------------------

set -u
set -o pipefail

# 1. Ensure we are in the project root
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_ROOT=$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel 2>/dev/null)
if [ -z "$PROJECT_ROOT" ]; then
    echo "Error: Unable to resolve the project root from $SCRIPT_DIR." >&2
    exit 1
fi
cd "$PROJECT_ROOT" || exit 1

STATE_FILE=".antlr_last_commit"
CURRENT_COMMIT=$(git rev-parse HEAD)
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
MAVEN_COMMAND="./mvnw"
# State format: active:<branch>:<commit> tracks generated output, branch:<branch>:<commit> preserves branch progress.
FORCE_REBUILD=false
ACTIVE_COMMIT=""
BRANCH_STATE_LINES=()

if [ ! -x "$MAVEN_COMMAND" ]; then
    MAVEN_COMMAND="mvn"
fi

print_usage() {
    echo "Usage: $0 [-f|--force]"
    echo
    echo "Options:"
    echo "  -f, --force    Rebuild all ANTLR4 modules and refresh state."
    echo "  -h, --help     Show this help message."
}

for each in "$@"; do
    case "$each" in
        -f|--force)
            FORCE_REBUILD=true
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Error: Unknown option '$each'." >&2
            print_usage >&2
            exit 1
            ;;
    esac
done

resolve_module_for_path() {
    local current_dir="$1"
    while [ "$current_dir" != "." ] && [ "$current_dir" != "/" ]; do
        if [ -f "$current_dir/pom.xml" ]; then
            printf "%s\n" "${current_dir#./}"
            return 0
        fi
        current_dir=$(dirname "$current_dir")
    done
    return 1
}

record_branch_state() {
    local record="$1"
    local branch=${record%%:*}
    local commit=${record#*:}
    if [ -n "$branch" ] && [ -n "$commit" ] && [ "$branch" != "$commit" ]; then
        BRANCH_STATE_LINES+=("$branch:$commit")
    fi
}

read_state_file() {
    local each
    if [ ! -f "$STATE_FILE" ]; then
        return 0
    fi
    while IFS= read -r each || [ -n "$each" ]; do
        case "$each" in
            active:*)
                ACTIVE_COMMIT=${each#active:}
                ACTIVE_COMMIT=${ACTIVE_COMMIT#*:}
                ;;
            branch:*)
                record_branch_state "${each#branch:}"
                ;;
            *:*)
                ACTIVE_COMMIT=${each#*:}
                record_branch_state "$each"
                ;;
        esac
    done < "$STATE_FILE"
}

is_valid_commit() {
    local commit="$1"
    [ -n "$commit" ] && git rev-parse --verify "$commit^{commit}" >/dev/null 2>&1
}

find_branch_commit() {
    local target_branch="$1"
    local result=""
    local each
    local branch
    local commit
    for each in "${BRANCH_STATE_LINES[@]}"; do
        branch=${each%%:*}
        commit=${each#*:}
        if [ "$target_branch" = "$branch" ]; then
            result="$commit"
        fi
    done
    if [ -n "$result" ]; then
        printf "%s\n" "$result"
    fi
}

resolve_last_commit() {
    local branch_commit
    if is_valid_commit "$ACTIVE_COMMIT"; then
        printf "%s\n" "$ACTIVE_COMMIT"
        return 0
    fi
    branch_commit=$(find_branch_commit "$CURRENT_BRANCH")
    if is_valid_commit "$branch_commit"; then
        printf "%s\n" "$branch_commit"
        return 0
    fi
    branch_commit=$(find_branch_commit "master")
    if is_valid_commit "$branch_commit"; then
        printf "%s\n" "$branch_commit"
    fi
}

write_state_file() {
    local temp_file
    local updated_current=false
    local each
    local branch
    local commit
    temp_file=$(mktemp "${STATE_FILE}.XXXXXX") || exit 1
    printf "active:%s:%s\n" "$CURRENT_BRANCH" "$CURRENT_COMMIT" > "$temp_file"
    for each in "${BRANCH_STATE_LINES[@]}"; do
        branch=${each%%:*}
        commit=${each#*:}
        if [ "$CURRENT_BRANCH" = "$branch" ]; then
            printf "branch:%s:%s\n" "$CURRENT_BRANCH" "$CURRENT_COMMIT" >> "$temp_file"
            updated_current=true
        else
            printf "branch:%s:%s\n" "$branch" "$commit" >> "$temp_file"
        fi
    done
    if [ "$updated_current" = "false" ]; then
        printf "branch:%s:%s\n" "$CURRENT_BRANCH" "$CURRENT_COMMIT" >> "$temp_file"
    fi
    mv "$temp_file" "$STATE_FILE"
}

# 2. Function to find all modules containing .g4 files
find_all_antlr_modules() {
    local all_modules=()
    local current_dir
    echo "Scanning all ANTLR4 modules... (This may take a moment)" >&2
    while IFS= read -r -d '' each; do
        current_dir=$(dirname "$each")
        if resolved_module=$(resolve_module_for_path "$current_dir"); then
            all_modules+=("$resolved_module")
        fi
    done < <(find . -name "*.g4" -not -path "*/target/*" -print0)
    if [ ${#all_modules[@]} -eq 0 ]; then
        return 0
    fi
    printf "%s\n" "${all_modules[@]}" | sort -u
}

collect_changed_g4_files() {
    local last_commit="$1"
    {
        git diff --name-only "$last_commit" "$CURRENT_COMMIT" -- '*.g4'
        git diff --name-only --cached -- '*.g4'
        git diff --name-only -- '*.g4'
        git ls-files --others --exclude-standard -- '*.g4'
    } | sort -u
}

# 3. Determine change set
read_state_file
if [ "$FORCE_REBUILD" = "true" ]; then
    echo "Force rebuild requested. Recompiling all ANTLR4 modules..." >&2
    changed_files="FORCE_ALL"
else
    LAST_COMMIT=$(resolve_last_commit)
    if [ -n "$LAST_COMMIT" ]; then
        echo "Branch: $CURRENT_BRANCH. Comparing grammar changes since $LAST_COMMIT..." >&2
        changed_files=$(collect_changed_g4_files "$LAST_COMMIT")
    else
        changed_files="FORCE_ALL"
    fi
fi

# 4. Resolve modules to recompile
if [ "$changed_files" = "FORCE_ALL" ]; then
    unique_modules=$(find_all_antlr_modules)
elif [ -n "$changed_files" ]; then
    modules_to_build=()
    echo "Detected changes in grammar files." >&2
    for file in $changed_files; do
        dir=$(dirname "$file")
        if resolved_module=$(resolve_module_for_path "$dir"); then
            modules_to_build+=("$resolved_module")
        fi
    done
    if [ ${#modules_to_build[@]} -eq 0 ]; then
        unique_modules=""
    else
        unique_modules=$(printf "%s\n" "${modules_to_build[@]}" | sort -u)
    fi
else
    echo "No ANTLR4 grammar changes detected."
    write_state_file
    exit 0
fi

# 5. Execute Maven build with optimized flags
if [ -n "$unique_modules" ]; then
    project_list=$(echo "$unique_modules" | tr '\n' ',' | sed 's/,$//')

    echo "------------------------------------------------"
    echo "Recompiling affected modules via '$MAVEN_COMMAND compile'..."
    echo "This ensures all imported grammars (Symbol, etc.) are correctly resolved."
    echo "------------------------------------------------"

    # -T 1C: Parallel build (1 thread per core)
    # -am: Also make dependencies (Crucial for Resource Copying)
    # -DskipTests, -Dcheckstyle.skip, etc: Skips heavy non-compilation tasks
    "$MAVEN_COMMAND" compile -pl "$project_list" -am \
        -T 1C \
        -DskipTests \
        -Dcheckstyle.skip \
        -Drat.skip \
        -Dmaven.javadoc.skip \
        -Djacoco.skip \
        -Dspotless.skip

    if [ $? -eq 0 ]; then
        write_state_file
        echo "------------------------------------------------"
        echo "SUCCESS: ANTLR4 classes and visitors are ready."
    else
        echo "ERROR: Maven build failed. You might need to run './mvnw install' on infra modules once." >&2
        exit 1
    fi
else
    echo "No relevant Maven modules found."
fi
