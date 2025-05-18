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

package org.apache.shardingsphere.infra.binder.context.statement.dal;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExplainStatementContextTest {
    
    @Test
    void assertNewInstance() {
        ExplainStatement explainStatement = mock(ExplainStatement.class);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(explainStatement.getSqlStatement()).thenReturn(sqlStatement);
        ExplainStatementContext actual = new ExplainStatementContext(mock(ShardingSphereMetaData.class), explainStatement, Collections.emptyList(), "foo_db");
        assertThat(actual.getSqlStatement(), is(explainStatement));
        assertThat(actual.getSqlStatement().getSqlStatement(), is(sqlStatement));
        assertThat(actual.getTablesContext().getSimpleTables(), is(Collections.emptyList()));
    }
}
