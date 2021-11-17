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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
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
    
    private final Set<String> columnNames = new LinkedHashSet<>();
    
    private final SelectStatement sqlStatement;
    
    private String databaseNameAlias = DATABASE_NAME;
    
    public SelectDatabaseExecutor(final SelectStatement sqlStatement, final String sql) {
        super(sql);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    protected void createPreProcessing() {
        LinkedList<String> schemaWithoutDataSource = ProxyContext.getInstance().getAllSchemaNames().stream()
                .filter(each -> !hasDatasource(each)).collect(Collectors.toCollection(LinkedList::new));
        schemaWithoutDataSource.forEach(each -> getRows().addLast(getDefaultRowData(each)));
    }
    
    @Override
    protected List<String> getSchemaNames() {
        Collection<String> schemaNames = ProxyContext.getInstance().getAllSchemaNames();
        return schemaNames.stream().filter(AbstractDatabaseMetadataExecutor::hasDatasource).collect(Collectors.toList());
    }
    
    @Override
    protected void rowPostProcessing(final String schemaName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        buildColumnNames(aliasMap);
        ShardingSphereResource resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource();
        Set<String> catalogs = resource.getDataSources().keySet().stream().map(each -> resource.getDataSourcesMetaData().getDataSourceMetaData(each).getCatalog()).collect(Collectors.toSet());
        databaseNameAlias = aliasMap.getOrDefault(DATABASE_NAME, "");
        String rowValue = rowMap.getOrDefault(databaseNameAlias, "").toString();
        if (catalogs.contains(rowValue)) {
            rowMap.replace(databaseNameAlias, schemaName);
        } else {
            rowMap.clear();
        }
    }
    
    private void buildColumnNames(final Map<String, String> aliasMap) {
        aliasMap.forEach((key, value) -> {
            if (!value.isEmpty()) {
                columnNames.add(value);
            } else {
                columnNames.add(key);
            }
        });
        
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
        } else {
            return projections.stream().map(each -> {
                ColumnProjectionSegment segment = (ColumnProjectionSegment) each;
                return segment.getAlias().isPresent() ? segment.getAlias().get() : segment.getColumn().getIdentifier().getValue();
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
