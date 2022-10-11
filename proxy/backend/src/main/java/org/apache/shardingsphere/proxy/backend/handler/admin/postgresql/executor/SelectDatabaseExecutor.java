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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor;

import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Database metadata query executor, used to query the schemata database.
 */
public final class SelectDatabaseExecutor extends DefaultDatabaseMetadataExecutor {
    
    private static final String DATABASE_NAME = "databasename";
    
    private static final String DATNAME = "datname";
    
    private static final String NAME = "name";
    
    private final Set<String> columnNames = new LinkedHashSet<>();
    
    private final SelectStatement sqlStatement;
    
    private String databaseNameAlias = DATABASE_NAME;
    
    private boolean isQueryDatabase;
    
    public SelectDatabaseExecutor(final SelectStatement sqlStatement, final String sql) {
        super(sql);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    protected void createPreProcessing() {
        removeDuplicatedRow();
        addDefaultRow();
    }
    
    private void addDefaultRow() {
        Collection<String> schemaWithoutDataSource = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> !hasDataSource(each)).collect(Collectors.toList());
        schemaWithoutDataSource.forEach(each -> getRows().addLast(getDefaultRowData(each)));
    }
    
    private void removeDuplicatedRow() {
        if (isQueryDatabase) {
            List<Map<String, Object>> toBeRemovedRow = getRows().stream().collect(Collectors.groupingBy(each -> each.get(databaseNameAlias), Collectors.toCollection(LinkedList::new)))
                    .values().stream().filter(each -> each.size() > 1).map(LinkedList::getLast).collect(Collectors.toList());
            toBeRemovedRow.forEach(each -> getRows().remove(each));
        }
    }
    
    @Override
    protected List<String> getDatabaseNames(final ConnectionSession connectionSession) {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> hasAuthority(each, connectionSession.getGrantee())).collect(Collectors.toList());
        return databaseNames.stream().filter(AbstractDatabaseMetadataExecutor::hasDataSource).collect(Collectors.toList());
    }
    
    @Override
    protected void rowPostProcessing(final String databaseName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        buildColumnNames(aliasMap);
        ShardingSphereResources resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResources();
        Set<String> catalogs = resource.getDataSources().keySet().stream().map(each -> resource.getDataSourceMetaData(each).getCatalog()).collect(Collectors.toSet());
        databaseNameAlias = aliasMap.getOrDefault(DATABASE_NAME, aliasMap.getOrDefault(DATNAME, aliasMap.getOrDefault(NAME, "")));
        String rowValue = rowMap.getOrDefault(databaseNameAlias, "").toString();
        isQueryDatabase = !rowValue.isEmpty();
        if (catalogs.contains(rowValue)) {
            rowMap.replace(databaseNameAlias, databaseName);
        } else {
            rowMap.clear();
        }
    }
    
    private void buildColumnNames(final Map<String, String> aliasMap) {
        aliasMap.forEach((key, value) -> columnNames.add(value.isEmpty() ? key : value));
        
    }
    
    private Map<String, Object> getDefaultRowData(final String schemaName) {
        Map<String, Object> result;
        if (columnNames.isEmpty()) {
            columnNames.addAll(getDefaultColumnNames());
        }
        result = columnNames.stream().collect(Collectors.toMap(each -> each, each -> ""));
        result.replace(databaseNameAlias, schemaName);
        return result;
    }
    
    private Set<String> getDefaultColumnNames() {
        Collection<ProjectionSegment> projections = sqlStatement.getProjections().getProjections();
        if (projections.stream().anyMatch(each -> !(each instanceof ColumnProjectionSegment))) {
            return Collections.singleton(databaseNameAlias);
        }
        return projections.stream().map(each -> {
            ColumnProjectionSegment segment = (ColumnProjectionSegment) each;
            return segment.getAlias().isPresent() ? segment.getAlias().get() : segment.getColumn().getIdentifier().getValue();
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
