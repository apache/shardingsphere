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

package org.apache.shardingsphere.core.parse.rule.registry;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.ShardingParsingRuleRegistry;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ParsingRuleRegistryTest {
    @Test
    public void assertFindSQLStatementRule() {
        assertTrue(ShardingParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "CreateTableContext").isPresent());
    }
    
    @Test
    public void assertNotFindSQLStatementRule() {
        assertFalse(ShardingParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "Invalid").isPresent());
    }
    
    @Test
    public void assertFindSQLStatementRuleWithH2() {
        assertTrue(ShardingParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.H2, "CreateTableContext").isPresent());
    }
    
    @Test
    public void assertFindSQLStatementFiller() {
        assertTrue(ShardingParsingRuleRegistry.getInstance().findSQLSegmentFiller(DatabaseType.MySQL, TableSegment.class).isPresent());
    }
    
    @Test
    public void assertNotFindSQLStatementFiller() {
        assertFalse(ShardingParsingRuleRegistry.getInstance().findSQLSegmentFiller(DatabaseType.MySQL, SQLSegment.class).isPresent());
    }
}
