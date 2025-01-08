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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterViewStatementContextTest {
    
    private SimpleTableSegment view;
    
    @BeforeEach
    void setUp() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("view"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        view = new SimpleTableSegment(tableNameSegment);
    }
    
    @Test
    void assertMySQLNewInstance() {
        SelectStatement select = mock(MySQLSelectStatement.class);
        when(select.getFrom()).thenReturn(Optional.of(view));
        when(select.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        MySQLAlterViewStatement alterViewStatement = new MySQLAlterViewStatement();
        alterViewStatement.setView(view);
        alterViewStatement.setSelect(select);
        assertNewInstance(alterViewStatement);
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        PostgreSQLAlterViewStatement alterViewStatement = new PostgreSQLAlterViewStatement();
        alterViewStatement.setView(view);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("view"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        alterViewStatement.setRenameView(new SimpleTableSegment(tableNameSegment));
        assertNewInstance(alterViewStatement);
    }
    
    private void assertNewInstance(final AlterViewStatement alterViewStatement) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        AlterViewStatementContext actual = new AlterViewStatementContext(metaData, Collections.emptyList(), alterViewStatement, "foo_db");
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterViewStatement));
        assertThat(actual.getTablesContext().getSimpleTables().size(), is(2));
    }
}
