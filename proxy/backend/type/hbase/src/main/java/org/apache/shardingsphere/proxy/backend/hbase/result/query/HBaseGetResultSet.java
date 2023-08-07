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

package org.apache.shardingsphere.proxy.backend.hbase.result.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverterFactory;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseSelectOperation;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseExecutor;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * HBase get result.
 */
@Slf4j
public final class HBaseGetResultSet implements HBaseQueryResultSet {
    
    private static final String ROW_KEY_COLUMN_NAME = "rowKey";
    
    private static final String CONTENT_COLUMN_NAME = "content";
    
    private static final String TIMESTAMP_COLUMN_NAME = "timestamp";
    
    private SelectStatementContext statementContext;
    
    private long resultNum;
    
    private long maxLimitResultSize;
    
    @Getter
    private Collection<String> columnNames = Collections.singleton(ROW_KEY_COLUMN_NAME);
    
    private Result compensateResult;
    
    private Iterator<Result> rows;
    
    /**
     * Init data.
     *
     * @param sqlStatementContext SQL statement context
     */
    @Override
    public void init(final SQLStatementContext sqlStatementContext) {
        statementContext = (SelectStatementContext) sqlStatementContext;
        initResultNum(sqlStatementContext);
        HBaseOperation operation = HBaseOperationConverterFactory.newInstance(sqlStatementContext).convert();
        long startMills = System.currentTimeMillis();
        if (operation.getOperation() instanceof Get) {
            executeGetRequest(operation);
        } else if (operation.getOperation() instanceof HBaseSelectOperation) {
            executeGetsRequest(operation);
        } else {
            executeScanRequest(operation);
        }
        logExecuteTime(startMills);
    }
    
    private void initResultNum(final SQLStatementContext sqlStatementContext) {
        resultNum = 0;
        maxLimitResultSize = HBaseContext.getInstance().getProps().<Long>getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE);
        Optional<PaginationValueSegment> paginationSegment = ((MySQLSelectStatement) sqlStatementContext.getSqlStatement()).getLimit().flatMap(LimitSegment::getRowCount);
        paginationSegment.ifPresent(optional -> maxLimitResultSize = Math.min(maxLimitResultSize, ((NumberLiteralLimitValueSegment) optional).getValue()));
    }
    
    private void executeGetRequest(final HBaseOperation operation) {
        Result result = HBaseExecutor.executeQuery(operation.getTableName(), table -> table.get((Get) operation.getOperation()));
        Collection<Result> rows = 0 == result.rawCells().length ? Collections.emptyList() : Collections.singleton(result);
        this.rows = rows.iterator();
        setColumnNames(this.rows);
    }
    
    private void executeGetsRequest(final HBaseOperation operation) {
        List<Result> results = Arrays.asList(HBaseExecutor.executeQuery(operation.getTableName(), table -> table.get(((HBaseSelectOperation) operation.getOperation()).getGets())));
        results = results.stream().filter(result -> result.rawCells().length > 0).collect(Collectors.toList());
        if (statementContext.getOrderByContext().isGenerated()) {
            results.sort(this::compareResult);
        }
        rows = results.iterator();
        setColumnNames(rows);
    }
    
    private int compareResult(final Result result1, final Result result2) {
        return Bytes.toString(result1.getRow()).compareTo(Bytes.toString(result2.getRow()));
    }
    
    private void executeScanRequest(final HBaseOperation hbaseOperation) {
        Scan scan = (Scan) hbaseOperation.getOperation();
        scan.setLimit((int) maxLimitResultSize);
        ResultScanner resultScanner = HBaseExecutor.executeQuery(hbaseOperation.getTableName(), table -> table.getScanner(scan));
        rows = resultScanner.iterator();
        setColumnNames(rows);
    }
    
    private void setColumnNames(final Iterator<Result> rows) {
        if (rows.hasNext()) {
            compensateResult = rows.next();
        }
        columnNames = null == compensateResult ? Arrays.asList(ROW_KEY_COLUMN_NAME, CONTENT_COLUMN_NAME) : parseResult(compensateResult).keySet();
    }
    
    private Map<String, String> parseResult(final Result result) {
        Map<String, String> row = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        row.put(ROW_KEY_COLUMN_NAME, Bytes.toString(result.getRow()));
        Long timestamp = null;
        for (Cell each : result.listCells()) {
            String column = new String(CellUtil.cloneQualifier(each), StandardCharsets.UTF_8);
            String value = new String(CellUtil.cloneValue(each), StandardCharsets.UTF_8);
            if (null == timestamp) {
                timestamp = each.getTimestamp();
            }
            row.put(column, value);
        }
        row.put(TIMESTAMP_COLUMN_NAME, String.valueOf(timestamp));
        return row;
    }
    
    private void logExecuteTime(final long startMills) {
        long endMills = System.currentTimeMillis();
        String tableName = statementContext.getSqlStatement().getFrom() instanceof SimpleTableSegment
                ? ((SimpleTableSegment) statementContext.getSqlStatement().getFrom()).getTableName().getIdentifier().getValue()
                : statementContext.getSqlStatement().getFrom().toString();
        String whereClause = getWhereClause();
        if (endMills - startMills > HBaseContext.getInstance().getProps().<Long>getValue(HBasePropertyKey.EXECUTE_TIME_OUT)) {
            log.info(String.format("query hbase table: %s,  where case: %s  ,  query %dms time out", tableName, whereClause, endMills - startMills));
        } else {
            log.info(String.format("query hbase table: %s,  where case: %s  ,  execute time: %dms", tableName, whereClause, endMills - startMills));
        }
    }
    
    private String getWhereClause() {
        if (!statementContext.getSqlStatement().getWhere().isPresent()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        ExpressionSegment expressionSegment = statementContext.getSqlStatement().getWhere().get().getExpr();
        if (expressionSegment instanceof BetweenExpression) {
            result.append(((BetweenExpression) expressionSegment).getBetweenExpr());
        } else if (expressionSegment instanceof BinaryOperationExpression) {
            result.append(((BinaryOperationExpression) expressionSegment).getText());
        }
        return result.toString();
    }
    
    @Override
    public boolean next() {
        return resultNum < maxLimitResultSize && (rows.hasNext() || compensateResult != null);
    }
    
    @Override
    public Collection<Object> getRowData() {
        Map<String, String> row;
        if (null == compensateResult) {
            row = parseResult(rows.next());
        } else {
            row = parseResult(compensateResult);
            compensateResult = null;
        }
        resultNum++;
        return columnNames.stream().map(each -> row.getOrDefault(each, "")).collect(Collectors.toList());
    }
    
    @Override
    public Class<MySQLSelectStatement> getType() {
        return MySQLSelectStatement.class;
    }
}
