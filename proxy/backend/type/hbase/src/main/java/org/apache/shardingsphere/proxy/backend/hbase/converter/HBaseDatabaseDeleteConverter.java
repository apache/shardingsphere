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
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HBase database delete converter.
 */
@RequiredArgsConstructor
public final class HBaseDatabaseDeleteConverter extends HBaseDatabaseRowKeysConverterAdapter implements HBaseDatabaseConverter {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    /**
     * Convert SQL statement to HBase operation.
     *
     * @return HBase operation
     */
    @Override
    public HBaseOperation convert() {
        DeleteStatementContext context = (DeleteStatementContext) sqlStatementContext;
        if (context.getWhereSegments().stream().findFirst().isPresent() && context.getWhereSegments().stream().findFirst().get().getExpr() instanceof InExpression) {
            return createDeleteOperationByUseIn(context);
        } else {
            return createDeleteOperationByOneRowKey(context);
        }
    }
    
    private HBaseOperation createDeleteOperationByOneRowKey(final DeleteStatementContext context) {
        String tableName = context.getTablesContext().getTableNames().iterator().next();
        Preconditions.checkArgument(context.getWhereSegments().stream().findFirst().isPresent(), "where segment is absent");
        String rowKey = getRowKeyFromWhereSegment(context.getWhereSegments().stream().findFirst().get().getExpr());
        return new HBaseOperation(tableName, getDeleteByRowKey(rowKey));
    }
    
    private HBaseOperation createDeleteOperationByUseIn(final DeleteStatementContext context) {
        String tableName = context.getTablesContext().getTableNames().iterator().next();
        List<String> rowKeys = getRowKeysFromWhereSegmentByIn((InExpression) context.getWhereSegments().stream().findFirst().get().getExpr());
        List<Delete> deletes = rowKeys.stream().map(this::getDeleteByRowKey).collect(Collectors.toList());
        return new HBaseOperation(tableName, new HBaseDeleteOperationAdapter(tableName, deletes));
    }
    
    private Delete getDeleteByRowKey(final String rowKey) {
        return new Delete(Bytes.toBytes(rowKey));
    }
    
    private String getRowKeyFromWhereSegment(final ExpressionSegment expressionSegment) {
        BinaryOperationExpression expression = (BinaryOperationExpression) expressionSegment;
        return String.valueOf(((LiteralExpressionSegment) expression.getRight()).getLiterals());
    }
}
