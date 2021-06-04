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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingUpdateStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingUpdateStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateUpdateModifyMultiTables() {
        UpdateStatement updateStatement = createUpdateStatement();
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        joinTableSegment.setRight(new SimpleTableSegment(0, 0, new IdentifierValue("order")));
        updateStatement.setTableSegment(joinTableSegment);
        SQLStatementContext<UpdateStatement> sqlStatementContext = new UpdateStatementContext(updateStatement);
        Collection<String> tableNames = Lists.newArrayList("order", "order_item");
        when(shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(tableNames);
        when(shardingRule.isAllBindingTables(tableNames)).thenReturn(true);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateUpdateWithoutShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()), Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateUpdateWithShardingKey() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()), Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateUpdateWithoutShardingKeyAndParameters() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(false);
        List<Object> parameters = Arrays.asList(1, 1);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()), parameters, mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertValidateUpdateWithShardingKeyAndShardingParameterEquals() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        List<Object> parameters = Arrays.asList(1, 1);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, new UpdateStatementContext(createUpdateStatementAndParameters(1)), parameters, mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateUpdateWithShardingKeyAndShardingParameterNotEquals() {
        when(shardingRule.isShardingColumn("id", "user")).thenReturn(true);
        List<Object> parameters = Arrays.asList(1, 1);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, new UpdateStatementContext(createUpdateStatementAndParameters(2)), parameters, mock(ShardingSphereSchema.class));
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new MySQLUpdateStatement();
        result.setTableSegment(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        result.setSetAssignment(
                new SetAssignmentSegment(0, 0, Collections.singletonList(new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("id")), new LiteralExpressionSegment(0, 0, "")))));
        return result;
    }
    
    private UpdateStatement createUpdateStatementAndParameters(final Object shardingColumnParameter) {
        UpdateStatement result = new MySQLUpdateStatement();
        result.setTableSegment(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        Collection<AssignmentSegment> assignments = Collections.singletonList(
                new AssignmentSegment(0, 0, new ColumnSegment(0, 0, new IdentifierValue("id")), new LiteralExpressionSegment(0, 0, shardingColumnParameter)));
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, assignments);
        result.setSetAssignment(setAssignmentSegment);
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(0, 0, 0);
        BinaryOperationExpression binaryOperationExpression = new BinaryOperationExpression(0, 0, left, right, "=", null);
        WhereSegment where = new WhereSegment(0, 0, binaryOperationExpression);
        result.setWhere(where);
        return result;
    }
}
