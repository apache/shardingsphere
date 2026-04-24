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

# 1. Ensure we are in the project root
PROJECT_ROOT=$(git rev-parse --show-toplevel)
if [ $? -ne 0 ]; then
    echo "Error: Not a git repository." >&2
    exit 1
fi
cd "$PROJECT_ROOT" || exit 1

STATE_FILE=".antlr_last_commit"
CURRENT_COMMIT=$(git rev-parse HEAD)
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 2. Function to find all modules containing .g4 files
find_all_antlr_modules() {
    echo "Scanning all ANTLR4 modules... (This may take a moment)" >&2
    g4_dirs=$(find . -name "*.g4" -not -path "*/target/*" | xargs -I {} dirname {} | sort -u)
    all_modules=()
    for dir in $g4_dirs; do
        current_dir="$dir"
        while [[ "$current_dir" != "." && "$current_dir" != "/" ]]; do
            if [ -f "$current_dir/pom.xml" ]; then
                all_modules+=("${current_dir#./}")
                break
            fi
            current_dir=$(dirname "$current_dir")
        done
    done
    printf "%s\n" "${all_modules[@]}" | sort -u
}

# 3. Determine change set
if [ -f "$STATE_FILE" ]; then
    read -r stored_data < "$STATE_FILE"
    LAST_BRANCH=$(echo "$stored_data" | cut -d':' -f1)
    LAST_COMMIT=$(echo "$stored_data" | cut -d':' -f2)

    if [ "$CURRENT_BRANCH" == "$LAST_BRANCH" ] && git rev-parse "$LAST_COMMIT" >/dev/null 2>&1; then
        echo "Branch: $CURRENT_BRANCH. Comparing changes since $LAST_COMMIT..." >&2
        changed_files=$(git diff --name-only "$LAST_COMMIT" "$CURRENT_COMMIT" | grep '\.g4$')
    else
        changed_files="FORCE_ALL"
    fi
else
    changed_files="FORCE_ALL"
fi

# 4. Resolve modules to recompile
if [ "$changed_files" == "FORCE_ALL" ]; then
    unique_modules=$(find_all_antlr_modules)
elif [ ! -z "$changed_files" ]; then
    echo "Detected changes in grammar files." >&2
    modules_to_build=()
    for file in $changed_files; do
        dir=$(dirname "$file")
        while [[ "$dir" != "." && "$dir" != "/" ]]; do
            if [ -f "$dir/pom.xml" ]; then
                modules_to_build+=("${dir#./}")
                break
            fi
            dir=$(dirname "$dir")
        done
    done
    unique_modules=$(printf "%s\n" "${modules_to_build[@]}" | sort -u)
else
    echo "No ANTLR4 grammar changes detected."
    echo "$CURRENT_BRANCH:$CURRENT_COMMIT" > "$STATE_FILE"
    exit 0
fi

# 5. Execute Maven build with optimized flags
if [ ! -z "$unique_modules" ]; then
    project_list=$(echo "$unique_modules" | tr '\n' ',' | sed 's/,$//')

    echo "------------------------------------------------"
    echo "Recompiling affected modules via 'mvn compile'..."
    echo "This ensures all imported grammars (Symbol, etc.) are correctly resolved."
    echo "------------------------------------------------"

    # -T 1C: Parallel build (1 thread per core)
    # -am: Also make dependencies (Crucial for Resource Copying)
    # -DskipTests, -Dcheckstyle.skip, etc: Skips heavy non-compilation tasks
    mvn compile -pl "$project_list" -am \
        -T 1C \
        -DskipTests \
        -Dcheckstyle.skip \
        -Drat.skip \
        -Dmaven.javadoc.skip \
        -Djacoco.skip \
        -Dspotless.skip

    if [ $? -eq 0 ]; then
        echo "$CURRENT_BRANCH:$CURRENT_COMMIT" > "$STATE_FILE"
        echo "------------------------------------------------"
        echo "SUCCESS: ANTLR4 classes and visitors are ready."
    else
        echo "ERROR: Maven build failed. You might need to run 'mvn install' on infra modules once." >&2
        exit 1
    fi
else
    echo "No relevant Maven modules found."
fi