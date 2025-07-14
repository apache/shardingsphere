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

package org.apache.shardingsphere.infra.binder.context.statement.type.dal;

import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AnalyzeTableStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstance() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_table")));
        AnalyzeTableStatement analyzeTableStatement = new AnalyzeTableStatement(Collections.singletonList(tableSegment));
        CommonSQLStatementContext actual = new CommonSQLStatementContext(databaseType, analyzeTableStatement);
        assertThat(actual.getSqlStatement(), is(analyzeTableStatement));
        assertThat(actual.getDatabaseType(), is(databaseType));
        assertThat(actual.getTablesContext().getTableNames().size(), is(1));
        assertThat(actual.getTablesContext().getTableNames().iterator().next(), is("foo_table"));
    }
    
    @Test
    void assertNewInstanceWithEmptyTables() {
        AnalyzeTableStatement analyzeTableStatement = new AnalyzeTableStatement(Collections.emptyList());
        CommonSQLStatementContext actual = new CommonSQLStatementContext(databaseType, analyzeTableStatement);
        assertThat(actual.getSqlStatement(), is(analyzeTableStatement));
        assertThat(actual.getDatabaseType(), is(databaseType));
        assertThat(actual.getTablesContext().getTableNames().size(), is(0));
    }
    
    @Test
    void assertGetTablesDirectly() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_table")));
        AnalyzeTableStatement analyzeTableStatement = new AnalyzeTableStatement(Collections.singletonList(tableSegment));
        assertThat(analyzeTableStatement.getTables().size(), is(1));
        assertThat(analyzeTableStatement.getTables().iterator().next().getTableName().getIdentifier().getValue(), is("foo_table"));
    }
}
