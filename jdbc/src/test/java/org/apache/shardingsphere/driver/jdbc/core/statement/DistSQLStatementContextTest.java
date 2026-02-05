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

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class DistSQLStatementContextTest {
    
    @Test
    void assertGetSqlStatement() {
        DistSQLStatement distSQLStatement = mock(DistSQLStatement.class);
        DistSQLStatementContext context = new DistSQLStatementContext(distSQLStatement);
        assertThat(context.getSqlStatement(), is(distSQLStatement));
    }
    
    @Test
    void assertGetTablesContext() {
        DistSQLStatement distSQLStatement = mock(DistSQLStatement.class);
        DistSQLStatementContext context = new DistSQLStatementContext(distSQLStatement);
        assertThat(context.getTablesContext(), is(notNullValue()));
        assertThat(context.getTablesContext().getSimpleTables().isEmpty(), is(true));
    }
}
