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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HBase database update converter.
 */
@RequiredArgsConstructor
public final class HBaseDatabaseUpdateConverter extends HBaseDatabaseRowKeysConverterAdapter implements HBaseDatabaseConverter {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    /**
     * Convert SQL statement to HBase operation.
     *
     * @return HBase operation
     */
    @Override
    public HBaseOperation convert() {
        UpdateStatementContext context = (UpdateStatementContext) sqlStatementContext;
        Preconditions.checkArgument(context.getWhereSegments().stream().findFirst().isPresent(), "Where segment is not present");
        if (context.getWhereSegments().stream().findFirst().get().getExpr() instanceof InExpression) {
            return createHBasePutsOperation(context);
        }
        return new HBaseOperation(context.getTablesContext().getTableNames().iterator().next(), createHBaseRequest(context));
    }
    
    private HBaseOperation createHBasePutsOperation(final UpdateStatementContext context) {
        List<String> rowKeys = getRowKeysFromWhereSegmentByIn((InExpression) context.getWhereSegments().stream().findFirst().get().getExpr());
        List<Put> puts = rowKeys.stream().map(this::getPutByRowKey).collect(Collectors.toList());
        for (Put put : puts) {
            addPutColumn(context, put);
        }
        return new HBaseOperation(context.getTablesContext().getTableNames().iterator().next(),
                new HBaseUpdateOperationAdapter(context.getTablesContext().getTableNames().iterator().next(), puts));
    }
    
    private Put getPutByRowKey(final String rowKey) {
        return new Put(Bytes.toBytes(rowKey));
    }
    
    private Put createHBaseRequest(final UpdateStatementContext context) {
        String rowKey = getRowKeyFromWhereSegment(context.getWhereSegments().stream().findFirst().get().getExpr());
        Put result = getPutByRowKey(rowKey);
        addPutColumn(context, result);
        return result;
    }
    
    private void addPutColumn(final UpdateStatementContext context, final Put put) {
        for (AssignmentSegment segment : getAssignmentSegments(context)) {
            String column = segment.getColumns().iterator().next().getIdentifier().getValue();
            LiteralExpressionSegment literalExpressionSegment = (LiteralExpressionSegment) segment.getValue();
            String value = String.valueOf(literalExpressionSegment.getLiterals());
            put.addColumn(Bytes.toBytes("i"), Bytes.toBytes(column), Bytes.toBytes(value));
        }
    }
    
    private Collection<AssignmentSegment> getAssignmentSegments(final UpdateStatementContext context) {
        return context.getSqlStatement().getSetAssignment().getAssignments();
    }
    
    private String getRowKeyFromWhereSegment(final ExpressionSegment expressionSegment) {
        BinaryOperationExpression expression = (BinaryOperationExpression) expressionSegment;
        return String.valueOf(((LiteralExpressionSegment) expression.getRight()).getLiterals());
    }
}
