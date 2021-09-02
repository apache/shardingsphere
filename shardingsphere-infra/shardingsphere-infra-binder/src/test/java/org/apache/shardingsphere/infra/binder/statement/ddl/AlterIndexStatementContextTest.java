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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterIndexStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterIndexStatementContextTest {

    @Test
    public void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLAlterIndexStatement.class));
    }

    @Test
    public void assertOracleNewInstance() {
        assertNewInstance(mock(OracleAlterIndexStatement.class));
    }

    @Test
    public void assertSQLServerNewInstance() {
        assertNewInstance(mock(SQLServerAlterIndexStatement.class));
    }

    private void assertNewInstance(final AlterIndexStatement alterIndexStatement) {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IdentifierValue("index_1"));
        when(alterIndexStatement.getIndex()).thenReturn(Optional.of(indexSegment));
        AlterIndexStatementContext actual = new AlterIndexStatementContext(alterIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterIndexStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.emptyList()));
        assertThat(actual.getIndexes().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("index_1")));
    }
}
