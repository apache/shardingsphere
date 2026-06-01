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

package org.apache.shardingsphere.database.connector.opengauss.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * openGauss provider of identifier case rules.
 */
public final class OpenGaussIdentifierCaseRuleProvider implements IdentifierCaseRuleProvider {
    
    @Override
    public Optional<IdentifierCaseRuleSet> provide(final IdentifierCaseRuleProviderContext context) {
        Objects.requireNonNull(context, "context cannot be null.");
        IdentifierCaseRuleSet lowerCaseRuleSet = IdentifierCaseRuleSets.newLowerCaseRuleSet();
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.SCHEMA, IdentifierCaseRuleSets.newInsensitiveRuleSet().getRule(IdentifierScope.SCHEMA));
        return Optional.of(new IdentifierCaseRuleSet(lowerCaseRuleSet.getRule(IdentifierScope.TABLE), scopedRules));
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
