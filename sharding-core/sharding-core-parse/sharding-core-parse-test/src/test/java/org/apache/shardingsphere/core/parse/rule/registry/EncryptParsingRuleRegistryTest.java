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
import org.apache.shardingsphere.core.parse.antlr.rule.registry.EncryptParsingRuleRegistry;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncryptParsingRuleRegistryTest {
    
    @Test
    public void assertFindSQLStatementRule() {
        assertTrue(EncryptParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "SelectContext").isPresent());
    }
    
    @Test
    public void assertFindNotSupportedSQLStatementRule() {
        assertFalse(EncryptParsingRuleRegistry.getInstance().findSQLStatementRule(DatabaseType.MySQL, "CreateTable").isPresent());
    }
    
    @Test
    public void assertFindSQLStatementFiller() {
        assertTrue(EncryptParsingRuleRegistry.getInstance().findSQLSegmentFiller(DatabaseType.MySQL, TableSegment.class).isPresent());
    }
    
    @Test
    public void assertFindNotSupportedSQLStatementFiller() {
        assertFalse(EncryptParsingRuleRegistry.getInstance().findSQLSegmentFiller(DatabaseType.MySQL, ColumnPositionSegment.class).isPresent());
    }
}
