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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.TableTokenGenerator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TableTokenGeneratorTest {
    
    @Test
    public void assertGenerateSQLToken() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findTableRule(anyString())).thenReturn(Optional.of(mock(TableRule.class)));
        TableTokenGenerator tableTokenGenerator = new TableTokenGenerator();
        tableTokenGenerator.setShardingRule(shardingRule);
        CreateDatabaseStatementContext createDatabaseStatementContext = mock(CreateDatabaseStatementContext.class);
        assertThat(tableTokenGenerator.generateSQLTokens(createDatabaseStatementContext), is(Collections.emptyList()));
        int testStartIndex = 3;
        TableNameSegment tableNameSegment = new TableNameSegment(testStartIndex, 8, new IdentifierValue("test"));
        CreateTableStatementContext createTableStatementContext = mock(CreateTableStatementContext.class);
        when(createTableStatementContext.getAllTables()).thenReturn(Collections.singleton(new SimpleTableSegment(tableNameSegment)));
        assertThat((new ArrayList<>(tableTokenGenerator.generateSQLTokens(createTableStatementContext))).get(0).getStartIndex(), is(testStartIndex));
    }
}
