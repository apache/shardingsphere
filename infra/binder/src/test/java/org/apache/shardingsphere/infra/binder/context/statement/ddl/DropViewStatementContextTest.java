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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropViewStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DropViewStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(new MySQLDropViewStatement());
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(new PostgreSQLDropViewStatement());
    }
    
    private void assertNewInstance(final DropViewStatement dropViewStatement) {
        TableNameSegment tableNameSegment1 = new TableNameSegment(0, 0, new IdentifierValue("tbl_1"));
        tableNameSegment1.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        TableNameSegment tableNameSegment2 = new TableNameSegment(0, 0, new IdentifierValue("tbl_2"));
        tableNameSegment2.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment table1 = new SimpleTableSegment(tableNameSegment1);
        SimpleTableSegment table2 = new SimpleTableSegment(tableNameSegment2);
        dropViewStatement.getViews().addAll(Arrays.asList(table1, table2));
        DropViewStatementContext actual = new DropViewStatementContext(dropViewStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(dropViewStatement));
    }
}
