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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropIndexStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DropIndexStatementContextTest {
    
    @Test
    public void assertMySQLNewInstance() {
        assertNewInstance(mock(MySQLDropIndexStatement.class));
    }
    
    @Test
    public void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLDropIndexStatement.class));
    }
    
    @Test
    public void assertOracleNewInstance() {
        assertNewInstance(mock(OracleDropIndexStatement.class));
    }
    
    @Test
    public void assertSQLServerNewInstance() {
        assertNewInstance(mock(SQLServerDropIndexStatement.class));
    }
    
    private void assertNewInstance(final DropIndexStatement dropIndexStatement) {
        Collection<IndexSegment> indexes = new LinkedList<>();
        IndexSegment index1 = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_1")));
        IndexSegment index2 = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_2")));
        indexes.add(index1);
        indexes.add(index2);
        when(dropIndexStatement.getIndexes()).thenReturn(indexes);
        DropIndexStatementContext actual = new DropIndexStatementContext(dropIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(dropIndexStatement));
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
        assertThat(actual.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("idx_1", "idx_2")));
    }
}
