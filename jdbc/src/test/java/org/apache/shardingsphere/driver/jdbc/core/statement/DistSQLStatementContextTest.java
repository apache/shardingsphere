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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistSQLStatementContextTest {
    
    @Test
    void assertGetSqlStatementForQueryableRAL() {
        ShowDistVariablesStatement statement = new ShowDistVariablesStatement(false, null);
        DistSQLStatementContext context = new DistSQLStatementContext(statement);
        assertThat(context.getSqlStatement(), CoreMatchers.is(statement));
    }
    
    @Test
    void assertGetSqlStatementForRUL() {
        ParseStatement statement = new ParseStatement("SELECT 1");
        DistSQLStatementContext context = new DistSQLStatementContext(statement);
        assertThat(context.getSqlStatement(), CoreMatchers.is(statement));
    }
    
    @Test
    void assertGetTablesContextForQueryableRAL() {
        DistSQLStatementContext context = new DistSQLStatementContext(new ShowDistVariablesStatement(false, null));
        assertThat(context.getTablesContext(), CoreMatchers.is(CoreMatchers.notNullValue()));
        assertTrue(context.getTablesContext().getSimpleTables().isEmpty());
    }
    
    @Test
    void assertGetTablesContextForRUL() {
        DistSQLStatementContext context = new DistSQLStatementContext(new ParseStatement("SELECT 1"));
        assertThat(context.getTablesContext(), CoreMatchers.is(CoreMatchers.notNullValue()));
        assertTrue(context.getTablesContext().getSimpleTables().isEmpty());
    }
}
