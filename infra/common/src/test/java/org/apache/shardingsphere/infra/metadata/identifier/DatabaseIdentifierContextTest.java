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

package org.apache.shardingsphere.infra.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.StandardIdentifierCaseRule;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DatabaseIdentifierContextTest {
    
    @Test
    void assertGetRule() {
        IdentifierCaseRule expectedRule = createLowerRule();
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(expectedRule));
        IdentifierCaseRule actualRule = context.getRule(IdentifierScope.TABLE);
        assertThat(actualRule, is(expectedRule));
    }
    
    @Test
    void assertRefresh() {
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createLowerRule()));
        IdentifierCaseRule expectedRule = createUpperRule();
        context.refresh(new IdentifierCaseRuleSet(expectedRule));
        IdentifierCaseRule actualRule = context.getRule(IdentifierScope.TABLE);
        assertThat(actualRule, is(expectedRule));
    }
    
    private IdentifierCaseRule createLowerRule() {
        return new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
    }
    
    private IdentifierCaseRule createUpperRule() {
        return new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toUpperCase(Locale.ENGLISH), each -> true);
    }
}
