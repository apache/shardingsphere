/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Derived alias alias.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DerivedAlias {
    
    AGGREGATION_DISTINCT_DERIVED("AGGREGATION_DISTINCT_DERIVED_");
    
    private final String pattern;
    
    /**
     * Get alias of derived alias.
     *
     * @param derivedAliasCount derived alias count
     * @return alias of derived alias
     */
    public String getDerivedAlias(final int derivedAliasCount) {
        return String.format(pattern + "%s", derivedAliasCount);
    }
    
    /**
     * Judge is derived alias or not.
     * 
     * @param aliasName alias name to be judged
     * @return is derived alias or not
     */
    public static boolean isDerivedAlias(final String aliasName) {
        for (DerivedAlias each : DerivedAlias.values()) {
            if (aliasName.startsWith(each.pattern)) {
                return true;
            }
        }
        return false;
    }
}
