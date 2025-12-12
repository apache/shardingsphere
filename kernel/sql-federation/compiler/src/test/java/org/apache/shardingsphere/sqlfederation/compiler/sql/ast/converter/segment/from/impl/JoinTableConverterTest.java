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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl;

import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExpressionConverter.class, ColumnConverter.class})
class JoinTableConverterTest {
    
    @Test
    void assertConvertReturnsCommaJoinWhenNoConditionOrUsing() {
        JoinTableSegment segment = new JoinTableSegment();
        segment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_left"))));
        segment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_right"))));
        SqlJoin actual = (SqlJoin) JoinTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getJoinType(), is(JoinType.COMMA));
        assertThat(actual.getConditionType(), is(JoinConditionType.NONE));
        assertNull(actual.getCondition());
        assertFalse(actual.isNatural());
    }
    
    @Test
    void assertConvertUsesProvidedCondition() {
        JoinTableSegment segment = new JoinTableSegment();
        segment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_left"))));
        segment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_right"))));
        segment.setJoinType(JoinType.LEFT.name());
        ExpressionSegment condition = mock(ExpressionSegment.class);
        SqlNode expectedCondition = mock(SqlNode.class);
        when(ExpressionConverter.convert(condition)).thenReturn(Optional.of(expectedCondition));
        segment.setCondition(condition);
        SqlJoin actual = (SqlJoin) JoinTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getJoinType(), is(JoinType.LEFT));
        assertThat(actual.getConditionType(), is(JoinConditionType.ON));
        assertThat(actual.getCondition(), is(expectedCondition));
    }
    
    @Test
    void assertConvertUsesUsingColumns() {
        JoinTableSegment segment = new JoinTableSegment();
        segment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_left"))));
        segment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_right"))));
        segment.setNatural(true);
        ColumnSegment usingColumn = new ColumnSegment(0, 0, new IdentifierValue("id"));
        segment.setUsing(new ArrayList<>(Collections.singletonList(usingColumn)));
        SqlIdentifier usingNode = mock(SqlIdentifier.class);
        when(ColumnConverter.convert(usingColumn)).thenReturn(usingNode);
        SqlJoin actual = (SqlJoin) JoinTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getJoinType(), is(JoinType.INNER));
        assertThat(actual.getConditionType(), is(JoinConditionType.USING));
        assertThat(actual.getCondition(), instanceOf(SqlNodeList.class));
        assertNotNull(actual.getCondition());
        assertThat(((SqlNodeList) actual.getCondition()).get(0), is(usingNode));
        assertTrue(actual.isNaturalNode().booleanValue());
    }
}
