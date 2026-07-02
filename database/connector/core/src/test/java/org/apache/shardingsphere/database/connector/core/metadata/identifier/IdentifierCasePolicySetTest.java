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

class IdentifierCasePolicySetTest {
    
    @Test
    void assertGetPolicy() {
        IdentifierCasePolicy expectedRule = new StandardIdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
        IdentifierCasePolicy actualRule = new IdentifierCasePolicySet(expectedRule).getPolicy(IdentifierScope.TABLE);
        assertThat(actualRule, is(expectedRule));
    }
    
    @Test
    void assertGetPolicyWithScopeOverride() {
        IdentifierCasePolicy defaultRule = new StandardIdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
        IdentifierCasePolicy schemaRule = new StandardIdentifierCasePolicy(LookupMode.EXACT, LookupMode.EXACT, each -> each, each -> true);
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.SCHEMA, schemaRule);
        IdentifierCasePolicySet actualPolicySet = new IdentifierCasePolicySet(defaultRule, scopedRules);
        assertThat(actualPolicySet.getPolicy(IdentifierScope.SCHEMA), is(schemaRule));
        assertThat(actualPolicySet.getPolicy(IdentifierScope.TABLE), is(defaultRule));
    }
}
