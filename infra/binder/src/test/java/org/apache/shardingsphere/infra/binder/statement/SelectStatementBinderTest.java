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

package org.apache.shardingsphere.infra.binder.statement;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectStatementBinderTest {
    
    @Test
    void assertBind() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projections);
        ColumnProjectionSegment orderIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        ColumnProjectionSegment userIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        ColumnProjectionSegment statusProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        projections.getProjections().add(orderIdProjection);
        projections.getProjections().add(userIdProjection);
        projections.getProjections().add(statusProjection);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        selectStatement.setFrom(simpleTableSegment);
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(selectStatement));
        assertThat(actual.getFrom(), not(selectStatement.getFrom()));
        assertThat(actual.getFrom(), instanceOf(SimpleTableSegment.class));
        assertThat(((SimpleTableSegment) actual.getFrom()).getTableName(), not(simpleTableSegment.getTableName()));
        assertThat(actual.getProjections(), not(selectStatement.getProjections()));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        assertThat(actualProjections, not(selectStatement.getProjections()));
        assertThat(actualProjections.get(0), not(orderIdProjection));
        assertThat(actualProjections.get(0), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(0)).getColumn(), not(orderIdProjection.getColumn()));
        assertThat(actualProjections.get(1), not(userIdProjection));
        assertThat(actualProjections.get(1), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(1)).getColumn(), not(userIdProjection.getColumn()));
        assertThat(actualProjections.get(2), not(statusProjection));
        assertThat(actualProjections.get(2), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(2)).getColumn(), not(statusProjection.getColumn()));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        return result;
    }
}
