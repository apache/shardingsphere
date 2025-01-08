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
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateViewStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateViewStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(new MySQLCreateViewStatement());
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(new PostgreSQLCreateViewStatement());
    }
    
    private void assertNewInstance(final CreateViewStatement createViewStatement) {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("view"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment view = new SimpleTableSegment(tableNameSegment);
        createViewStatement.setView(view);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        createViewStatement.setSelect(selectStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        CreateViewStatementContext actual = new CreateViewStatementContext(metaData, Collections.emptyList(), createViewStatement, "foo_db");
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(createViewStatement));
    }
}
