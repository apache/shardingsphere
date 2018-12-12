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

package io.shardingsphere.core.parsing.antlr.rule.registry;

import io.shardingsphere.core.constant.DatabaseType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DatabaseRuleDefinitionTypeTest {
    
    @Test
    public void assertValueOf() {
        assertThat(DatabaseRuleDefinitionType.valueOf(DatabaseType.MySQL), is(DatabaseRuleDefinitionType.MySQL));
        assertThat(DatabaseRuleDefinitionType.valueOf(DatabaseType.PostgreSQL), is(DatabaseRuleDefinitionType.PostgreSQL));
        assertThat(DatabaseRuleDefinitionType.valueOf(DatabaseType.Oracle), is(DatabaseRuleDefinitionType.Oracle));
        assertThat(DatabaseRuleDefinitionType.valueOf(DatabaseType.SQLServer), is(DatabaseRuleDefinitionType.SQLServer));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInvalidValueOf() {
        DatabaseRuleDefinitionType.valueOf(DatabaseType.H2);
    }
}
