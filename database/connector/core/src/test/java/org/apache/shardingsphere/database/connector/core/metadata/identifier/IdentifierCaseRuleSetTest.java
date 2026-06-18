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

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IdentifierCaseRuleSetTest {
    
    @Test
    void assertGetRule() {
        IdentifierCaseRule expectedRule = new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
        IdentifierCaseRule actualRule = new IdentifierCaseRuleSet(expectedRule).getRule(IdentifierScope.TABLE);
        assertThat(actualRule, is(expectedRule));
    }
    
    @Test
    void assertGetRuleWithScopeOverride() {
        IdentifierCaseRule defaultRule = new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
        IdentifierCaseRule schemaRule = new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.EXACT, each -> each, each -> true);
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.SCHEMA, schemaRule);
        IdentifierCaseRuleSet actualRuleSet = new IdentifierCaseRuleSet(defaultRule, scopedRules);
        assertThat(actualRuleSet.getRule(IdentifierScope.SCHEMA), is(schemaRule));
        assertThat(actualRuleSet.getRule(IdentifierScope.TABLE), is(defaultRule));
    }
}
