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

package org.apache.shardingsphere.proxy.backend.exception;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.exception.fixture.TestSQLStatementContextInstanceOfTableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableModifyInTransactionExceptionTest {

    @Test
    public void assertTableNameWhenSQLStatementContextInstanceOfSQLStatementContextUnImplementsTableAvailable() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        TableModifyInTransactionException tableModifyInTransactionException = new TableModifyInTransactionException(sqlStatementContext);
        assertThat(tableModifyInTransactionException.getTableName(), is("unknown_table"));
    }

    @Test
    public void assertTableNameWhenSQLStatementContextInstanceOfSQLStatementContextImplementsTableAvailable() {
        SQLStatementContext sqlStatementContext = mock(TestSQLStatementContextInstanceOfTableAvailable.class);
        TableModifyInTransactionException tableModifyInTransactionException = new TableModifyInTransactionException(sqlStatementContext);
        assertThat(tableModifyInTransactionException.getTableName(), is("unknown_table"));
    }

    @Test
    public void assertTableNameWhenSQLStatementContextInstanceOfSQLStatementContextImplementsTableAvailableOnEmptyTableList() {
        TestSQLStatementContextInstanceOfTableAvailable sqlStatementContext = mock(TestSQLStatementContextInstanceOfTableAvailable.class);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.emptyList());
        TableModifyInTransactionException tableModifyInTransactionException = new TableModifyInTransactionException(sqlStatementContext);
        assertThat(tableModifyInTransactionException.getTableName(), is("unknown_table"));
    }

    @Test
    public void assertTableNameWhenSQLStatementContextInstanceOfSQLStatementContextImplementsTableAvailableOnAnyTableList() {
        TestSQLStatementContextInstanceOfTableAvailable sqlStatementContext = mock(TestSQLStatementContextInstanceOfTableAvailable.class);
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        IdentifierValue identifierValue = mock(IdentifierValue.class);
        when(identifierValue.getValue()).thenReturn("identifierValue");
        TableNameSegment tableNameSegment = new TableNameSegment(0, 1, identifierValue);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.singleton(simpleTableSegment));
        TableModifyInTransactionException tableModifyInTransactionException = new TableModifyInTransactionException(sqlStatementContext);
        assertThat(tableModifyInTransactionException.getTableName(), is("identifierValue"));
    }
}
