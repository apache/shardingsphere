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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection;

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ColumnProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ExpressionProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ShorthandProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.SubqueryProjectionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        ColumnProjectionConverter.class, ExpressionProjectionConverter.class, ShorthandProjectionConverter.class,
        SubqueryProjectionConverter.class, AggregationProjectionConverter.class, ParameterMarkerExpressionConverter.class
})
class ProjectionsConverterTest {
    
    @Test
    void assertConvertCollectsAllSupportedProjections() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SqlNode expectedColumnNode = mock(SqlNode.class);
        ColumnProjectionSegment columnProjectionSegment = mock(ColumnProjectionSegment.class);
        when(ColumnProjectionConverter.convert(columnProjectionSegment)).thenReturn(expectedColumnNode);
        projectionsSegment.getProjections().add(columnProjectionSegment);
        SqlNode expectedExpressionNode = mock(SqlNode.class);
        ExpressionProjectionSegment expressionProjectionSegment = mock(ExpressionProjectionSegment.class);
        when(ExpressionProjectionConverter.convert(expressionProjectionSegment)).thenReturn(Optional.of(expectedExpressionNode));
        projectionsSegment.getProjections().add(expressionProjectionSegment);
        SqlNode expectedShorthandNode = mock(SqlNode.class);
        ShorthandProjectionSegment shorthandProjectionSegment = mock(ShorthandProjectionSegment.class);
        when(ShorthandProjectionConverter.convert(shorthandProjectionSegment)).thenReturn(Optional.of(expectedShorthandNode));
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        SqlNode expectedSubqueryNode = mock(SqlNode.class);
        SubqueryProjectionSegment subqueryProjectionSegment = mock(SubqueryProjectionSegment.class);
        when(SubqueryProjectionConverter.convert(subqueryProjectionSegment)).thenReturn(Optional.of(expectedSubqueryNode));
        projectionsSegment.getProjections().add(subqueryProjectionSegment);
        SqlNode expectedAggregationNode = mock(SqlNode.class);
        AggregationProjectionSegment aggregationProjectionSegment = mock(AggregationProjectionSegment.class);
        when(AggregationProjectionConverter.convert(aggregationProjectionSegment)).thenReturn(Optional.of(expectedAggregationNode));
        projectionsSegment.getProjections().add(aggregationProjectionSegment);
        SqlDynamicParam expectedParameterNode = mock(SqlDynamicParam.class);
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        when(ParameterMarkerExpressionConverter.convert(parameterMarkerExpressionSegment)).thenReturn(expectedParameterNode);
        projectionsSegment.getProjections().add(parameterMarkerExpressionSegment);
        SqlNodeList actual = ProjectionsConverter.convert(projectionsSegment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is(expectedColumnNode));
        assertThat(actual.get(1), is(expectedExpressionNode));
        assertThat(actual.get(2), is(expectedShorthandNode));
        assertThat(actual.get(3), is(expectedSubqueryNode));
        assertThat(actual.get(4), is(expectedAggregationNode));
        assertThat(actual.get(5), is(expectedParameterNode));
    }
    
    @Test
    void assertConvertSkipsUnsupportedProjection() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ProjectionSegment unsupportedProjection = mock(ProjectionSegment.class);
        projectionsSegment.getProjections().add(unsupportedProjection);
        SqlNodeList actual = ProjectionsConverter.convert(projectionsSegment).orElse(null);
        assertNotNull(actual);
        assertFalse(actual.iterator().hasNext());
    }
}
