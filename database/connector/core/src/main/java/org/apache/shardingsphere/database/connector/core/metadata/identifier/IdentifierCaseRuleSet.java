/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Identifier case rules for one database.
 */
public final class IdentifierCaseRuleSet {
    
    private final IdentifierCaseRule defaultRule;
    
    private final Map<IdentifierScope, IdentifierCaseRule> scopedRules;
    
    public IdentifierCaseRuleSet(final IdentifierCaseRule defaultRule) {
        this(defaultRule, Collections.emptyMap());
    }
    
    public IdentifierCaseRuleSet(final IdentifierCaseRule defaultRule, final Map<IdentifierScope, IdentifierCaseRule> scopedRules) {
        this.defaultRule = Objects.requireNonNull(defaultRule, "defaultRule cannot be null.");
        Map<IdentifierScope, IdentifierCaseRule> actualScopedRules = new EnumMap<>(IdentifierScope.class);
        actualScopedRules.putAll(Objects.requireNonNull(scopedRules, "scopedRules cannot be null."));
        this.scopedRules = Collections.unmodifiableMap(actualScopedRules);
    }
    
    /**
     * Get rule for identifier scope.
     *
     * @param identifierScope identifier scope
     * @return identifier case rule
     */
    public IdentifierCaseRule getRule(final IdentifierScope identifierScope) {
        Objects.requireNonNull(identifierScope, "identifierScope cannot be null.");
        return scopedRules.getOrDefault(identifierScope, defaultRule);
    }
}
