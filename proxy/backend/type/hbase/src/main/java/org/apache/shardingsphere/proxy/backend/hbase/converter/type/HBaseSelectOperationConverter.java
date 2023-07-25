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
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Query;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseRowKeyExtractor;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseSelectOperation;
import org.apache.shardingsphere.proxy.backend.hbase.util.HBaseHeterogeneousUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HBase select operation converter.
 */
@RequiredArgsConstructor
@Slf4j
public final class HBaseSelectOperationConverter implements HBaseOperationConverter {
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public HBaseOperation convert() {
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        Optional<WhereSegment> whereSegment = selectStatementContext.getWhereSegments().stream().findFirst();
        Preconditions.checkArgument(whereSegment.isPresent(), "Where segment is absent.");
        return whereSegment.get().getExpr() instanceof BinaryOperationExpression || whereSegment.get().getExpr() instanceof InExpression
                ? createGetOperation(selectStatementContext, whereSegment.get())
                : createScanOperation(selectStatementContext, whereSegment.get());
    }
    
    private HBaseOperation createGetOperation(final SelectStatementContext selectStatementContext, final WhereSegment whereSegment) {
        ExpressionSegment expr = whereSegment.getExpr();
        List<Get> gets = getRowKeys(expr).stream().map(each -> new Get(Bytes.toBytes(each))).collect(Collectors.toList());
        if (!HBaseHeterogeneousUtils.isUseShorthandProjection(selectStatementContext)) {
            for (Get each : gets) {
                appendColumns(each, selectStatementContext);
            }
        }
        String tableName = selectStatementContext.getTablesContext().getTableNames().iterator().next();
        return expr instanceof InExpression ? new HBaseOperation(tableName, new HBaseSelectOperation(gets)) : new HBaseOperation(tableName, gets.get(0));
    }
    
    private List<String> getRowKeys(final ExpressionSegment expr) {
        return expr instanceof InExpression ? HBaseRowKeyExtractor.getRowKeys((InExpression) expr) : Collections.singletonList(HBaseRowKeyExtractor.getRowKey((BinaryOperationExpression) expr));
    }
    
    private HBaseOperation createScanOperation(final SelectStatementContext selectStatementContext, final WhereSegment whereSegment) {
        Scan scan = new Scan();
        if (whereSegment.getExpr() instanceof BetweenExpression) {
            appendBetween(scan, whereSegment.getExpr(), false);
        }
        if (!HBaseHeterogeneousUtils.isUseShorthandProjection(selectStatementContext)) {
            appendColumns(scan, selectStatementContext);
        }
        appendLimit(scan, selectStatementContext);
        return new HBaseOperation(selectStatementContext.getTablesContext().getTableNames().iterator().next(), scan);
    }
    
    private void appendColumns(final Query query, final SelectStatementContext selectStatementContext) {
        if (query instanceof Get) {
            selectStatementContext.getColumnSegments().forEach(each -> ((Get) query).addColumn(Bytes.toBytes("i"), Bytes.toBytes(String.valueOf(each))));
        } else {
            selectStatementContext.getColumnSegments().forEach(each -> ((Scan) query).addColumn(Bytes.toBytes("i"), Bytes.toBytes(String.valueOf(each))));
        }
    }
    
    private void appendBetween(final Scan scan, final ExpressionSegment expressionSegment, final boolean reversed) {
        BetweenExpression betweenExpr = (BetweenExpression) expressionSegment;
        String startRowKey = ((LiteralExpressionSegment) betweenExpr.getBetweenExpr()).getLiterals().toString();
        String stopRowKey = ((LiteralExpressionSegment) betweenExpr.getAndExpr()).getLiterals().toString();
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
    
    private void appendLimit(final Scan scan, final SelectStatementContext selectStatementContext) {
        // TODO consider about other dialect
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) selectStatementContext.getSqlStatement();
        if (selectStatement.getLimit().isPresent()) {
            selectStatement.getLimit().get().getRowCount().ifPresent(optional -> scan.setLimit((int) ((NumberLiteralLimitValueSegment) optional).getValue()));
        }
    }
}
