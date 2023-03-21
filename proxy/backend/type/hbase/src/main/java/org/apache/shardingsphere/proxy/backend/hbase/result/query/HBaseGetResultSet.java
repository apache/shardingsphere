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

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
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
 * Result get for HBase.
 */
@Slf4j
public final class HBaseGetResultSet implements HBaseQueryResultSet {
    
    private SelectStatementContext statementContext;
    
    private Collection<String> columns = Collections.singleton("rowKey");
    
    private Result compensateResult;
    
    private Iterator<Result> iterator;
    
    private long resultNum;
    
    private long maxLimitResultSize;
    
    /**
     * Init data.
     *
     * @param sqlStatementContext SQL statement context.
     */
    @Override
    public void init(final SQLStatementContext<?> sqlStatementContext) {
        statementContext = (SelectStatementContext) sqlStatementContext;
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation hbaseOperation = converter.convert();
        initResultNum(sqlStatementContext);
        final long startMill = System.currentTimeMillis();
        if (hbaseOperation.getOperation() instanceof Get) {
            executeGetRequest(hbaseOperation);
        } else if (hbaseOperation.getOperation() instanceof HBaseSelectOperation) {
            executeGetsRequest(hbaseOperation);
        } else {
            executeScanRequest(hbaseOperation);
        }
        
        final long endMill = System.currentTimeMillis();
        
        printExecuteTime(endMill, startMill);
    }
    
    private void printExecuteTime(final long endMill, final long startMill) {
        String hbTable;
        
        if (statementContext.getSqlStatement().getFrom() instanceof SimpleTableSegment) {
            hbTable = ((SimpleTableSegment) statementContext.getSqlStatement().getFrom()).getTableName().getIdentifier().getValue();
        } else {
            hbTable = statementContext.getSqlStatement().getFrom().toString();
        }
        
        String whereCase = "";
        
        if (statementContext.getSqlStatement().getWhere().isPresent()) {
            ExpressionSegment expressionSegment = statementContext.getSqlStatement().getWhere().get().getExpr();
            if (expressionSegment instanceof BetweenExpression) {
                whereCase += ((BetweenExpression) expressionSegment).getBetweenExpr();
            } else if (expressionSegment instanceof BinaryOperationExpression) {
                whereCase += ((BinaryOperationExpression) expressionSegment).getText();
            }
        }
        if (endMill - startMill > HBaseContext.getInstance().getProps().<Long>getValue(HBasePropertyKey.EXECUTE_TIME_OUT)) {
            log.info(String.format("query hbase table: %s,  where case: %s  ,  query %dms time out", hbTable, whereCase, endMill - startMill));
        } else {
            log.info(String.format("query hbase table: %s,  where case: %s  ,  execute time: %dms", hbTable, whereCase, endMill - startMill));
        }
    }
    
    private void initResultNum(final SQLStatementContext<?> sqlStatementContext) {
        resultNum = 0;
        maxLimitResultSize = HBaseContext.getInstance().getProps().<Long>getValue(HBasePropertyKey.MAX_SCAN_LIMIT_SIZE);
        Optional<PaginationValueSegment> paginationSegment = ((MySQLSelectStatement) sqlStatementContext.getSqlStatement()).getLimit().flatMap(LimitSegment::getRowCount);
        paginationSegment.ifPresent(valueSegment -> maxLimitResultSize = Math.min(maxLimitResultSize, ((NumberLiteralLimitValueSegment) valueSegment).getValue()));
    }
    
    private void executeGetsRequest(final HBaseOperation hbaseOperation) {
        List<Result> results = Arrays.asList(HBaseExecutor.executeQuery(hbaseOperation.getTableName(), table -> table.get(((HBaseSelectOperation) hbaseOperation.getOperation()).getGets())));
        results = results.stream().filter(result -> result.rawCells().length > 0).collect(Collectors.toList());
        orderResults(results);
        iterator = results.iterator();
        setColumns(iterator);
    }
    
    private void orderResults(final List<Result> results) {
        if (!this.statementContext.getOrderByContext().isGenerated()) {
            return;
        }
        results.sort(this::compareResult);
    }
    
    private int compareResult(final Result result1, final Result result2) {
        return Bytes.toString(result1.getRow()).compareTo(Bytes.toString(result2.getRow()));
    }
    
    private void executeGetRequest(final HBaseOperation hbaseOperation) {
        Result result = HBaseExecutor.executeQuery(hbaseOperation.getTableName(), table -> table.get((Get) hbaseOperation.getOperation()));
        List<Result> rows = 0 == result.rawCells().length ? Collections.emptyList() : Collections.singletonList(result);
        iterator = rows.iterator();
        setColumns(iterator);
    }
    
    private void executeScanRequest(final HBaseOperation hbaseOperation) {
        Scan scan = (Scan) hbaseOperation.getOperation();
        scan.setLimit(Long.valueOf(maxLimitResultSize).intValue());
        ResultScanner resultScanner = HBaseExecutor.executeQuery(hbaseOperation.getTableName(), table -> table.getScanner(scan));
        iterator = resultScanner.iterator();
        setColumns(iterator);
    }
    
    private void setColumns(final Iterator<Result> iterator) {
        if (iterator.hasNext()) {
            compensateResult = iterator.next();
        }
        if (compensateResult != null) {
            Map<String, String> row = parseResult(compensateResult);
            columns = row.keySet();
        } else {
            columns = Arrays.asList("rowKey", "content");
        }
    }
    
    private Map<String, String> parseResult(final Result result) {
        Map<String, String> row = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        row.put("rowKey", Bytes.toString(result.getRow()));
        Long timestamp = null;
        for (Cell cell : result.listCells()) {
            String column = new String(CellUtil.cloneQualifier(cell), StandardCharsets.UTF_8);
            String value = new String(CellUtil.cloneValue(cell), StandardCharsets.UTF_8);
            cell.getTimestamp();
            if (timestamp == null) {
                timestamp = cell.getTimestamp();
            }
            row.put(column, value);
        }
        row.put("timestamp", String.valueOf(timestamp));
        return row;
    }
    
    /**
     * Get result set column names.
     *
     * @return result set column names.
     */
    @Override
    public Collection<String> getColumnNames() {
        return columns;
    }
    
    /**
     * Go to next data.
     *
     * @return true if next data exist.
     */
    @Override
    public boolean next() {
        return resultNum < maxLimitResultSize && (iterator.hasNext() || compensateResult != null);
    }
    
    /**
     * Get row data.
     *
     * @return row data.
     */
    @Override
    public Collection<Object> getRowData() {
        Map<String, String> row;
        if (compensateResult != null) {
            row = parseResult(compensateResult);
            compensateResult = null;
        } else {
            row = parseResult(iterator.next());
        }
        resultNum++;
        return columns.stream().map(each -> row.getOrDefault(each, "")).collect(Collectors.toList());
    }
    
    /**
     * Get Type.
     *
     * @return Type Name.
     */
    @Override
    public String getType() {
        return MySQLSelectStatement.class.getCanonicalName();
    }
}
