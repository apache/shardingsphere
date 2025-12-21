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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.MySQLShowTransactionStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show transaction executor for MySQL.
 */
@RequiredArgsConstructor
public class MySQLShowTransactionExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowTransactionStatement sqlStatement;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    private Optional<Long> filterTransactionId = Optional.empty();
    
    private Optional<String> filterLabel = Optional.empty();
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String databaseName = getDatabaseName(connectionSession);
        if (null != databaseName) {
            ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        }
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new LocalDataMergedResult(getQueryResultRows(databaseName, metaData));
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession) {
        return null == sqlStatement.getFromDatabase() ? connectionSession.getUsedDatabaseName() : sqlStatement.getFromDatabase().getDatabase().getIdentifier().getValue();
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>(15);
        columns.add(new RawQueryResultColumnMetaData("", "TransactionId", "TransactionId", Types.BIGINT, "BIGINT", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Label", "Label", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Coordinator", "Coordinator", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "TransactionStatus", "TransactionStatus", Types.VARCHAR, "VARCHAR", 50, 0));
        columns.add(new RawQueryResultColumnMetaData("", "LoadJobSourceType", "LoadJobSourceType", Types.VARCHAR, "VARCHAR", 50, 0));
        columns.add(new RawQueryResultColumnMetaData("", "PrepareTime", "PrepareTime", Types.VARCHAR, "VARCHAR", 50, 0));
        columns.add(new RawQueryResultColumnMetaData("", "CommitTime", "CommitTime", Types.VARCHAR, "VARCHAR", 50, 0));
        columns.add(new RawQueryResultColumnMetaData("", "FinishTime", "FinishTime", Types.VARCHAR, "VARCHAR", 50, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Reason", "Reason", Types.VARCHAR, "VARCHAR", 1000, 0));
        columns.add(new RawQueryResultColumnMetaData("", "ErrorReplicasCount", "ErrorReplicasCount", Types.INTEGER, "INTEGER", 10, 0));
        columns.add(new RawQueryResultColumnMetaData("", "ListenerId", "ListenerId", Types.BIGINT, "BIGINT", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "TimeoutMs", "TimeoutMs", Types.BIGINT, "BIGINT", 20, 0));
        return new RawQueryResultMetaData(columns);
    }
    
    private Collection<LocalDataQueryResultRow> getQueryResultRows(final String databaseName, final ShardingSphereMetaData metaData) {
        if (null == databaseName) {
            return Collections.emptyList();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (!database.isComplete()) {
            return Collections.emptyList();
        }
        extractWhereFilter();
        Collection<TransactionInfo> transactions = loadTransactions();
        return transactions.stream()
                .filter(this::matchesFilter)
                .map(this::buildTransactionRow)
                .collect(Collectors.toList());
    }
    
    private void extractWhereFilter() {
        if (null == sqlStatement.getWhere()) {
            return;
        }
        if (sqlStatement.getWhere().getExpr() instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpr = (BinaryOperationExpression) sqlStatement.getWhere().getExpr();
            extractFilterFromBinaryExpression(binaryExpr);
        }
    }
    
    private void extractFilterFromBinaryExpression(final BinaryOperationExpression expression) {
        if (!(expression.getLeft() instanceof ColumnSegment) || !(expression.getRight() instanceof LiteralExpressionSegment)) {
            return;
        }
        ColumnSegment column = (ColumnSegment) expression.getLeft();
        LiteralExpressionSegment literal = (LiteralExpressionSegment) expression.getRight();
        String columnName = column.getIdentifier().getValue().toLowerCase();
        Object literalValue = literal.getLiterals();
        if ("id".equalsIgnoreCase(columnName)) {
            filterTransactionId = extractLongValue(literalValue);
        } else if ("label".equalsIgnoreCase(columnName)) {
            filterLabel = Optional.of(String.valueOf(literalValue));
        }
    }
    
    private Optional<Long> extractLongValue(final Object value) {
        if (value instanceof Number) {
            return Optional.of(((Number) value).longValue());
        }
        if (value instanceof String) {
            try {
                return Optional.of(Long.parseLong((String) value));
            } catch (final NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private boolean matchesFilter(final TransactionInfo transaction) {
        if (filterTransactionId.isPresent() && filterTransactionId.get() != transaction.getTransactionId()) {
            return false;
        }
        if (filterLabel.isPresent() && !filterLabel.get().equals(transaction.getLabel())) {
            return false;
        }
        return true;
    }
    
    private LocalDataQueryResultRow buildTransactionRow(final TransactionInfo transaction) {
        return new LocalDataQueryResultRow(
                transaction.getTransactionId(),
                transaction.getLabel(),
                transaction.getCoordinator(),
                transaction.getTransactionStatus(),
                transaction.getLoadJobSourceType(),
                transaction.getPrepareTime(),
                transaction.getCommitTime(),
                transaction.getFinishTime(),
                transaction.getReason(),
                transaction.getErrorReplicasCount(),
                transaction.getListenerId(),
                transaction.getTimeoutMs());
    }
    
    protected Collection<TransactionInfo> loadTransactions() {
        throw new UnsupportedOperationException("SHOW TRANSACTION is not supported for the moment. ");
    }
    
    /**
     * Transaction information holder.
     */
    @Getter
    @RequiredArgsConstructor
    static class TransactionInfo {
        
        private final long transactionId;
        
        private final String label;
        
        private final String coordinator;
        
        private final String transactionStatus;
        
        private final String loadJobSourceType;
        
        private final String prepareTime;
        
        private final String commitTime;
        
        private final String finishTime;
        
        private final String reason;
        
        private final int errorReplicasCount;
        
        private final long listenerId;
        
        private final long timeoutMs;
    }
}
