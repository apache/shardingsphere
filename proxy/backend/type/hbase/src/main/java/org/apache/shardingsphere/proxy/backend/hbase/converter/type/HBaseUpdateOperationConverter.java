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

package org.apache.shardingsphere.proxy.backend.hbase.converter.type;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseRowKeyExtractor;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseUpdateOperation;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HBase update operation converter.
 */
@RequiredArgsConstructor
public final class HBaseUpdateOperationConverter implements HBaseOperationConverter {
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public HBaseOperation convert() {
        UpdateStatementContext updateStatementContext = (UpdateStatementContext) sqlStatementContext;
        Optional<WhereSegment> whereSegment = updateStatementContext.getWhereSegments().stream().findFirst();
        Preconditions.checkArgument(whereSegment.isPresent(), "Where segment is absent.");
        return whereSegment.get().getExpr() instanceof InExpression
                ? createUpdateMultipleRowKeysOperation(updateStatementContext, whereSegment.get())
                : createUpdateSingleRowKeyOperation(updateStatementContext, whereSegment.get());
    }
    
    private HBaseOperation createUpdateMultipleRowKeysOperation(final UpdateStatementContext updateStatementContext, final WhereSegment whereSegment) {
        String tableName = updateStatementContext.getTablesContext().getTableNames().iterator().next();
        List<Put> puts = HBaseRowKeyExtractor.getRowKeys((InExpression) whereSegment.getExpr()).stream().map(each -> new Put(Bytes.toBytes(each))).collect(Collectors.toList());
        for (Put each : puts) {
            addPutColumn(updateStatementContext, each);
        }
        return new HBaseOperation(tableName, new HBaseUpdateOperation(puts));
    }
    
    private HBaseOperation createUpdateSingleRowKeyOperation(final UpdateStatementContext updateStatementContext, final WhereSegment whereSegment) {
        String tableName = updateStatementContext.getTablesContext().getTableNames().iterator().next();
        return new HBaseOperation(tableName, createOperationRequest(updateStatementContext, whereSegment));
    }
    
    private Put createOperationRequest(final UpdateStatementContext updateStatementContext, final WhereSegment whereSegment) {
        Put result = new Put(Bytes.toBytes(HBaseRowKeyExtractor.getRowKey((BinaryOperationExpression) whereSegment.getExpr())));
        addPutColumn(updateStatementContext, result);
        return result;
    }
    
    private void addPutColumn(final UpdateStatementContext updateStatementContext, final Put put) {
        for (AssignmentSegment each : updateStatementContext.getSqlStatement().getSetAssignment().getAssignments()) {
            String columnName = each.getColumns().iterator().next().getIdentifier().getValue();
            String value = String.valueOf(((LiteralExpressionSegment) each.getValue()).getLiterals());
            put.addColumn(Bytes.toBytes("i"), Bytes.toBytes(columnName), Bytes.toBytes(value));
        }
    }
}
