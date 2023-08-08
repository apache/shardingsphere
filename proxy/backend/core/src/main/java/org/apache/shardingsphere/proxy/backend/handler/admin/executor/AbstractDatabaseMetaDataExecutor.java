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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The abstract class of database meta data, used to define the template.
 */
@Getter
public abstract class AbstractDatabaseMetaDataExecutor implements DatabaseAdminQueryExecutor {
    
    private QueryResultMetaData queryResultMetaData;
    
    private MergedResult mergedResult;
    
    private final List<Map<String, Object>> rows = new LinkedList<>();
    
    private final Collection<String> labels = new LinkedList<>();
    
    @Override
    public final void execute(final ConnectionSession connectionSession) throws SQLException {
        Collection<String> databaseNames = getDatabaseNames(connectionSession);
        for (String databaseName : databaseNames) {
            initDatabaseData(databaseName);
            processMetaData(databaseName, resultSet -> handleResultSet(databaseName, resultSet));
        }
        postProcess();
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = createMergedResult();
    }
    
    @SneakyThrows(SQLException.class)
    private void handleResultSet(final String databaseName, final ResultSet resultSet) {
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            Map<String, String> aliasMap = new LinkedHashMap<>();
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                aliasMap.put(metaData.getColumnName(i), metaData.getColumnLabel(i));
                rowMap.put(metaData.getColumnLabel(i), resultSet.getString(i));
            }
            preProcess(databaseName, rowMap, aliasMap);
            if (!rowMap.isEmpty()) {
                rows.add(rowMap);
            }
        }
        if (rows.isEmpty()) {
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                labels.add(metaData.getColumnLabel(i));
            }
        }
    }
    
    protected abstract void initDatabaseData(String databaseName);
    
    protected abstract Collection<String> getDatabaseNames(ConnectionSession connectionSession);
    
    protected abstract void preProcess(String databaseName, Map<String, Object> rows, Map<String, String> alias);
    
    protected abstract void postProcess();
    
    protected abstract void processMetaData(String databaseName, Consumer<ResultSet> callback) throws SQLException;
    
    private MergedResult createMergedResult() {
        List<MemoryQueryResultDataRow> resultDataRows = rows.stream().map(each -> new MemoryQueryResultDataRow(new LinkedList<>(each.values()))).collect(Collectors.toList());
        return new TransparentMergedResult(new RawMemoryQueryResult(queryResultMetaData, resultDataRows));
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        if (rows.isEmpty() && !labels.isEmpty()) {
            List<RawQueryResultColumnMetaData> columns = labels.stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
            return new RawQueryResultMetaData(columns);
        }
        List<RawQueryResultColumnMetaData> columns = rows.stream().flatMap(each -> each.keySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0)).collect(Collectors.toList());
        return new RawQueryResultMetaData(columns);
    }
    
    protected static Boolean hasDataSource(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).containsDataSource();
    }
    
    protected static boolean isAuthorized(final String databaseName, final Grantee grantee) {
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        return new AuthorityChecker(authorityRule, grantee).isAuthorized(databaseName);
    }
    
    /**
     * Default database meta data executor, execute sql directly in the database to obtain the result source data.
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class DefaultDatabaseMetaDataExecutor extends AbstractDatabaseMetaDataExecutor {
        
        private final String sql;
        
        private final List<Object> parameters;
        
        @Override
        protected void initDatabaseData(final String databaseName) {
        }
        
        @Override
        protected Collection<String> getDatabaseNames(final ConnectionSession connectionSession) {
            Optional<String> database = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> isAuthorized(each, connectionSession.getGrantee()))
                    .filter(AbstractDatabaseMetaDataExecutor::hasDataSource).findFirst();
            return database.map(Collections::singletonList).orElse(Collections.emptyList());
        }
        
        @Override
        protected void processMetaData(final String databaseName, final Consumer<ResultSet> callback) throws SQLException {
            ResourceMetaData resourceMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData();
            Optional<Entry<String, DataSource>> dataSourceEntry = resourceMetaData.getDataSources().entrySet().stream().findFirst();
            if (!dataSourceEntry.isPresent()) {
                return;
            }
            try (
                    Connection connection = dataSourceEntry.get().getValue().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    callback.accept(resultSet);
                }
            }
        }
        
        @Override
        protected void preProcess(final String databaseName, final Map<String, Object> rows, final Map<String, String> alias) {
        }
        
        @Override
        protected void postProcess() {
        }
    }
}
