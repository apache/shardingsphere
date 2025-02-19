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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.with;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WithSegmentBinderTest {
    
    @Test
    void assertBind() {
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionSegment = new ProjectionsSegment(42, 48);
        ColumnSegment columnSegment = new ColumnSegment(42, 48, new IdentifierValue("user_id"));
        projectionSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        mySQLSelectStatement.setProjections(projectionSegment);
        mySQLSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(55, 57, new IdentifierValue("cte"))));
        MySQLSelectStatement subqueryMySQLSelectStatement = new MySQLSelectStatement();
        ProjectionsSegment subqueryProjectionSegment = new ProjectionsSegment(20, 20);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(20, 20);
        subqueryProjectionSegment.getProjections().add(shorthandProjectionSegment);
        subqueryMySQLSelectStatement.setProjections(subqueryProjectionSegment);
        subqueryMySQLSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(27, 32, new IdentifierValue("t_user"))));
        SubquerySegment subquerySegment = new SubquerySegment(5, 33, subqueryMySQLSelectStatement, "(SELECT * FROM t_user)");
        Collection<CommonTableExpressionSegment> commonTableExpressionSegments = new LinkedList<>();
        commonTableExpressionSegments.add(new CommonTableExpressionSegment(5, 33, new AliasSegment(5, 7, new IdentifierValue("cte")), subquerySegment));
        WithSegment withSegment = new WithSegment(0, 33, commonTableExpressionSegments);
        mySQLSelectStatement.setWithSegment(withSegment);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), mySQLSelectStatement);
        WithSegment actual = WithSegmentBinder.bind(withSegment, binderContext, binderContext.getExternalTableBinderContexts());
        assertThat(binderContext.getExternalTableBinderContexts().size(), is(1));
        assertThat(binderContext.getCommonTableExpressionsSegmentsUniqueAliases().size(), is(1));
        assertThat(actual.getStartIndex(), is(0));
        assertTrue(binderContext.getExternalTableBinderContexts().containsKey(new CaseInsensitiveString("cte")));
        assertTrue(actual.getCommonTableExpressions().iterator().next().getAliasName().isPresent());
        assertThat(actual.getCommonTableExpressions().iterator().next().getAliasName().get(), is("cte"));
        assertThat(binderContext.getCommonTableExpressionsSegmentsUniqueAliases().iterator().next(), is("cte"));
        SimpleTableSegmentBinderContext simpleTableSegment = (SimpleTableSegmentBinderContext) binderContext.getExternalTableBinderContexts().get(new CaseInsensitiveString("cte")).iterator().next();
        ArrayList<ColumnProjectionSegment> columnProjectionSegments = new ArrayList<>();
        simpleTableSegment.getProjectionSegments().forEach(each -> columnProjectionSegments.add((ColumnProjectionSegment) each));
        assertThat(columnProjectionSegments.get(0).getColumn().getIdentifier().getValue(), is("user_id"));
        assertThat(columnProjectionSegments.get(1).getColumn().getIdentifier().getValue(), is("name"));
        assertTrue(columnProjectionSegments.get(1).getColumn().getOwner().isPresent());
        assertThat(columnProjectionSegments.get(1).getColumn().getOwner().get().getIdentifier().getValue(), is("t_user"));
        assertTrue(columnProjectionSegments.get(0).getColumn().getOwner().isPresent());
        assertThat(columnProjectionSegments.get(0).getColumn().getOwner().get().getIdentifier().getValue(), is("t_user"));
        assertThat(columnProjectionSegments.get(0).getColumn().getIdentifier().getValue(), is("user_id"));
        assertThat(columnProjectionSegments.get(1).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(columnProjectionSegments.get(0).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(actual.getCommonTableExpressions().iterator().next().getSubquery().getText(), is("(SELECT * FROM t_user)"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_user").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_user")).thenReturn(true);
        return result;
    }
}
