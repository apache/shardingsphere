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

package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowColumnsStatementContextTest {
    
    @Test
    public void assertNewInstance() {
        MySQLShowColumnsStatement mySQLShowColumnsStatement = mock(MySQLShowColumnsStatement.class);
        String tableName = "tbl_1";
        String databaseName = "sharding_db";
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        FromSchemaSegment fromSchema = new FromSchemaSegment(0, 0, new DatabaseSegment(0, 0, new IdentifierValue(databaseName)));
        when(mySQLShowColumnsStatement.getTable()).thenReturn(table);
        when(mySQLShowColumnsStatement.getFromSchema()).thenReturn(Optional.of(fromSchema));
        ShowColumnsStatementContext actual = new ShowColumnsStatementContext(mySQLShowColumnsStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(mySQLShowColumnsStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList(tableName)));
        assertThat(actual.getRemoveSegments(), is(Collections.singletonList(fromSchema)));
    }
}
