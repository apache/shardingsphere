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
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ParsingRuleRegistryTest {
    
    @Test
    public void assertFindSQLStatementRule() {
        assertTrue(ParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "CreateTableContext").isPresent());
    }
    
    @Test
    public void assertNotFindSQLStatementRule() {
        assertFalse(ParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "Invalid").isPresent());
    }
    
    @Test
    public void assertFindSQLStatementRuleWithH2() {
        assertTrue(ParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.H2, "CreateTableContext").isPresent());
    }
    
    @Test
    public void assertFindSQLStatementFiller() {
        assertTrue(ParsingRuleRegistry.getInstance().findSQLStatementFiller(TableSegment.class).isPresent());
    }
    
    @Test
    public void assertNotFindSQLStatementFiller() {
        assertFalse(ParsingRuleRegistry.getInstance().findSQLStatementFiller(SQLSegment.class).isPresent());
    }
}
