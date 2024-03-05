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

package org.apache.shardingsphere.infra.rule.identifier.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Rule identifiers.
 */
public final class RuleIdentifiers {
    
    private final Collection<RuleIdentifier> identifiers;
    
    public RuleIdentifiers(final RuleIdentifier... identifiers) {
        this.identifiers = Arrays.asList(identifiers);
    }
    
    /**
     * Find rule identifier.
     * 
     * @param ruleIdentifierClass rule identifier class
     * @param <T> type of rule identifier
     * @return found rule identifier
     */
    @SuppressWarnings("unchecked")
    public <T extends RuleIdentifier> Optional<T> findIdentifier(final Class<T> ruleIdentifierClass) {
        for (RuleIdentifier each : identifiers) {
            if (ruleIdentifierClass.isAssignableFrom(each.getClass())) {
                return Optional.of((T) each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get rule identifier.
     *
     * @param ruleIdentifierClass rule identifier class
     * @param <T> type of rule identifier
     * @return got rule identifier
     */
    public <T extends RuleIdentifier> T getIdentifier(final Class<T> ruleIdentifierClass) {
        return findIdentifier(ruleIdentifierClass).orElseThrow(() -> new IllegalStateException(String.format("Can not find rule identifier: %s", ruleIdentifierClass)));
    }
}
