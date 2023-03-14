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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Query;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.util.HeterogeneousUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HBase database select converter.
 */
@RequiredArgsConstructor
@Slf4j
public final class HBaseDatabaseSelectConverter extends HBaseDatabaseRowKeysConverterAdapter implements HBaseDatabaseConverter {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    /**
     * Convert SQL statement to HBase operation.
     *
     * @return HBase operation
     */
    @Override
    public HBaseOperation convert() {
        SelectStatementContext context = (SelectStatementContext) sqlStatementContext;
        if (isUseGetRequest(context)) {
            return createGetRequest(context);
        } else {
            return createScanRequest(context);
        }
    }
    
    private boolean isUseGetRequest(final SelectStatementContext context) {
        return context.getWhereSegments().stream().findFirst().isPresent()
                && (context.getWhereSegments().stream().findFirst().get().getExpr() instanceof BinaryOperationExpression
                        || context.getWhereSegments().stream().findFirst().get().getExpr() instanceof InExpression);
    }
    
    private List<String> getRowKeyFromWhereSegment(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof InExpression) {
            InExpression expression = (InExpression) expressionSegment;
            return getRowKeysFromWhereSegmentByIn(expression);
        } else {
            BinaryOperationExpression expression = (BinaryOperationExpression) expressionSegment;
            return new ArrayList<>(Collections.singleton(String.valueOf(((LiteralExpressionSegment) expression.getRight()).getLiterals())));
        }
    }
    
    private HBaseOperation createGetRequest(final SelectStatementContext context) {
        ExpressionSegment expression = context.getWhereSegments().stream().findFirst().get().getExpr();
        List<Get> gets = getRowKeyFromWhereSegment(expression).stream().map(this::getGetByRowKey).collect(Collectors.toList());
        if (!HeterogeneousUtil.isUseShorthandProjection(context)) {
            for (Get each : gets) {
                decorateWithColumns(each, context);
            }
        }
        if (expression instanceof InExpression) {
            return new HBaseOperation(context.getTablesContext().getTableNames().iterator().next(),
                    new HBaseSelectOperationAdapter(context.getTablesContext().getTableNames().iterator().next(), gets));
        }
        return new HBaseOperation(context.getTablesContext().getTableNames().iterator().next(), gets.get(0));
    }
    
    private Get getGetByRowKey(final String rowKey) {
        return new Get(Bytes.toBytes(rowKey));
    }
    
    private void decorateWithColumns(final Query query, final SelectStatementContext statementContext) {
        Collection<ColumnSegment> columns = statementContext.getColumnSegments();
        
        if (query instanceof Get) {
            columns.forEach(each -> ((Get) query).addColumn(Bytes.toBytes("i"), Bytes.toBytes(String.valueOf(each))));
        } else {
            columns.forEach(each -> ((Scan) query).addColumn(Bytes.toBytes("i"), Bytes.toBytes(String.valueOf(each))));
        }
    }
    
    private void decoratedWithLimit(final Scan scan, final SelectStatementContext statementContext) {
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) statementContext.getSqlStatement();
        if (selectStatement.getLimit().isPresent()) {
            Optional<PaginationValueSegment> paginationValueSegment = selectStatement.getLimit().get().getRowCount();
            paginationValueSegment.ifPresent(valueSegment -> scan.setLimit((int) ((NumberLiteralLimitValueSegment) valueSegment).getValue()));
        }
    }
    
    private HBaseOperation createScanRequest(final SelectStatementContext context) {
        Scan scan = new Scan();
        Optional<WhereSegment> whereSegment = context.getWhereSegments().stream().findFirst();
        if (whereSegment.isPresent() && whereSegment.get().getExpr() instanceof BetweenExpression) {
            decorateScanOperationWithBetweenExpression(scan, whereSegment.get().getExpr(), false);
        }
        if (!HeterogeneousUtil.isUseShorthandProjection(context)) {
            decorateWithColumns(scan, context);
        }
        decoratedWithLimit(scan, context);
        return new HBaseOperation(context.getTablesContext().getTableNames().iterator().next(), scan);
    }
    
    private void decorateScanOperationWithBetweenExpression(final Scan scan, final ExpressionSegment expressionSegment, final boolean reversed) {
        BetweenExpression betweenExpression = (BetweenExpression) expressionSegment;
        LiteralExpressionSegment betweenExpr = (LiteralExpressionSegment) betweenExpression.getBetweenExpr();
        LiteralExpressionSegment andExpr = (LiteralExpressionSegment) betweenExpression.getAndExpr();
        String startRowKey = betweenExpr.getLiterals().toString();
        String stopRowKey = andExpr.getLiterals().toString();
        if (null != startRowKey && null != stopRowKey) {
            if (reversed) {
                scan.withStopRow(calBytes(startRowKey, 0), true);
                // refer: <https://github.com/apache/hbase/blob/master/hbase-backup/src/main/java/org/apache/hadoop/hbase/backup/impl/BackupSystemTable.java#L1853>
                scan.withStartRow(calBytes(stopRowKey + "~", 0), true);
            } else {
                scan.withStartRow(calBytes(startRowKey, 0), true);
                scan.withStopRow(calBytes(stopRowKey + "~", 0), true);
            }
        }
    }
    
    private byte[] calBytes(final String row, final int step) {
        byte[] rowByte = Bytes.toBytes(row);
        byte[] result = Arrays.copyOf(rowByte, rowByte.length);
        result[result.length - 1] = (byte) (result[result.length - 1] + step);
        return result;
    }
}
