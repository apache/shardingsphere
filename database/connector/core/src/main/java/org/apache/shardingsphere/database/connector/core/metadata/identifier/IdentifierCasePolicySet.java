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

/**
 * Identifier case policies for one database.
 */
public final class IdentifierCasePolicySet {
    
    private final IdentifierCasePolicy defaultPolicy;
    
    private final Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies;
    
    public IdentifierCasePolicySet(final IdentifierCasePolicy defaultPolicy) {
        this(defaultPolicy, Collections.emptyMap());
    }
    
    public IdentifierCasePolicySet(final IdentifierCasePolicy defaultPolicy, final Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies) {
        this.defaultPolicy = defaultPolicy;
        Map<IdentifierScope, IdentifierCasePolicy> actualScopedPolicies = new EnumMap<>(IdentifierScope.class);
        actualScopedPolicies.putAll(scopedPolicies);
        this.scopedPolicies = Collections.unmodifiableMap(actualScopedPolicies);
    }
    
    /**
     * Get policy for identifier scope.
     *
     * @param identifierScope identifier scope
     * @return identifier case policy
     */
    public IdentifierCasePolicy getPolicy(final IdentifierScope identifierScope) {
        return scopedPolicies.getOrDefault(identifierScope, defaultPolicy);
    }
}
