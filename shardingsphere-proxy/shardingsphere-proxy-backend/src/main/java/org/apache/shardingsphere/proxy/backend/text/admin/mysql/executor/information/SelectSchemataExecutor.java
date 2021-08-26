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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.information;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.enums.InformationSchemataEnum;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schemata query executor.
 */
@Slf4j
public final class SelectSchemataExecutor implements DatabaseAdminQueryExecutor {
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    private final Map<String, String> initResultSetMap;
    
    private final Map<String, Map<String, String>> schemaMap = new HashMap<>();
    
    private final String sql;
    
    public SelectSchemataExecutor(final SelectStatement sqlStatement, final String sql) {
        this.sql = sql;
        Collection<ProjectionSegment> projections = sqlStatement.getProjections().getProjections();
        checkSegment(projections);
        initResultSetMap = isShorthandSegment(projections) ? initResultSetMap() : initResultSetMap(projections);
    }
    
    private void checkSegment(final Collection<ProjectionSegment> projections) {
        if (!isShorthandSegment(projections) && projections.stream().anyMatch(each -> !(each instanceof ColumnProjectionSegment))) {
            throw new UnsupportedOperationException(String.format("unsupported SQL : %s ", sql));
        }
    }
    
    private Boolean isShorthandSegment(final Collection<ProjectionSegment> projections) {
        return projections.stream().anyMatch(each -> each instanceof ShorthandProjectionSegment);
    }
    
    private Map<String, String> initResultSetMap(final Collection<ProjectionSegment> projections) {
        return projections.stream().map(each -> {
            IdentifierValue identifier = ((ColumnProjectionSegment) each).getColumn().getIdentifier();
            return identifier.getValue();
        }).collect(Collectors.toMap(each -> each, each -> ""));
    }
    
    private Map<String, String> initResultSetMap() {
        return Arrays.stream(InformationSchemataEnum.values()).map(Enum::name).collect(Collectors.toMap(each -> each, each -> ""));
    }
    
    @Override
    public void execute(final BackendConnection backendConnection) throws SQLException {
        for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
            Map<String, String> resultSetMap = new HashMap<>(initResultSetMap);
            schemaMap.put(each, resultSetMap);
            ShardingSphereResource resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(each).getResource();
            Optional<Entry<String, DataSource>> dataSourceEntry = resource.getDataSources().entrySet().stream().findFirst();
            if (!dataSourceEntry.isPresent()) {
                continue;
            }
            String catalog = resource.getDataSourcesMetaData().getDataSourceMetaData(dataSourceEntry.get().getKey()).getCatalog();
            log.info("Actual SQL: {} ::: {}", dataSourceEntry.get().getKey(), sql);
            // TODO Splicing where catalog?
            ResultSet resultSet = dataSourceEntry.get().getValue().getConnection().prepareStatement(sql).executeQuery();
            while (resultSet.next()) {
                String actualDatabaseName = resultSet.getString(InformationSchemataEnum.SCHEMA_NAME.name());
                if (!catalog.equals(actualDatabaseName)) {
                    continue;
                }
                putInResultSetMap(resultSetMap, resultSet);
                break;
            }
        }
        mergedResult = new TransparentMergedResult(getQueryResult());
        queryResultMetaData = createQueryResultMetaData();
    }
    
    private void putInResultSetMap(final Map<String, String> resultSetMap, final ResultSet resultSet) throws SQLException {
        for (String each : resultSetMap.keySet()) {
            resultSetMap.put(each, resultSet.getString(each));
        }
    }
    
    private RawQueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = initResultSetMap.keySet().stream()
                .map(each -> new RawQueryResultColumnMetaData("", each, each, Types.VARCHAR, "VARCHAR", 20, 0))
                .collect(Collectors.toList());
        return new RawQueryResultMetaData(columns);
    }
    
    private QueryResult getQueryResult() {
        List<MemoryQueryResultDataRow> rows = schemaMap.entrySet().stream()
                .map(this::replaceQueryResults)
                .map(each -> new MemoryQueryResultDataRow(new ArrayList<>(each.getValue().values())))
                .collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private Entry<String, Map<String, String>> replaceQueryResults(final Entry<String, Map<String, String>> entry) {
        entry.getValue().forEach((key, value) -> {
            if (InformationSchemataEnum.SCHEMA_NAME.name().equalsIgnoreCase(key)) {
                entry.getValue().put(InformationSchemataEnum.SCHEMA_NAME.name(), entry.getKey());
            }
        });
        return entry;
    }
}
