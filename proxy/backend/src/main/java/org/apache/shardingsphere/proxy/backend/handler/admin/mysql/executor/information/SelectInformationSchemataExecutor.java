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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.information;

import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Schemata query executor, used to query the schemata table.
 */
public final class SelectInformationSchemataExecutor extends DefaultDatabaseMetadataExecutor {
    
    public static final String SCHEMA_NAME = "SCHEMA_NAME";
    
    public static final String DEFAULT_CHARACTER_SET_NAME = "DEFAULT_CHARACTER_SET_NAME";
    
    public static final String DEFAULT_COLLATION_NAME = "DEFAULT_COLLATION_NAME";
    
    public static final String CATALOG_NAME = "CATALOG_NAME";
    
    public static final String SQL_PATH = "SQL_PATH";
    
    public static final String DEFAULT_ENCRYPTION = "DEFAULT_ENCRYPTION";
    
    private static final Set<String> SCHEMA_WITHOUT_DATA_SOURCE = new LinkedHashSet<>();
    
    private final SelectStatement sqlStatement;
    
    private String schemaNameAlias = SCHEMA_NAME;
    
    private boolean queryDatabase;
    
    public SelectInformationSchemataExecutor(final SelectStatement sqlStatement, final String sql) {
        super(sql);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    protected void createPreProcessing() {
        removeDuplicatedRow();
    }
    
    private void removeDuplicatedRow() {
        if (queryDatabase) {
            List<Map<String, Object>> reservedRow = getRows().stream().collect(Collectors.groupingBy(each -> each.get(schemaNameAlias), Collectors.toCollection(LinkedList::new)))
                    .values().stream().map(LinkedList::getFirst).collect(Collectors.toList());
            reservedRow.forEach(each -> getRows().removeIf(row -> !getRows().contains(each)));
        }
    }
    
    @Override
    protected List<String> getDatabaseNames(final ConnectionSession connectionSession) {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> hasAuthority(each, connectionSession.getGrantee())).collect(Collectors.toList());
        SCHEMA_WITHOUT_DATA_SOURCE.addAll(databaseNames.stream().filter(each -> !AbstractDatabaseMetadataExecutor.hasDataSource(each)).collect(Collectors.toSet()));
        List<String> result = databaseNames.stream().filter(AbstractDatabaseMetadataExecutor::hasDataSource).collect(Collectors.toList());
        if (!SCHEMA_WITHOUT_DATA_SOURCE.isEmpty()) {
            fillSchemasWithoutDataSource();
        }
        return result;
    }
    
    @Override
    protected void rowPostProcessing(final String databaseName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        ShardingSphereResource resource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResource();
        Set<String> catalogs = resource.getDataSources().keySet().stream().map(each -> resource.getDataSourceMetaData(each).getCatalog()).collect(Collectors.toSet());
        schemaNameAlias = aliasMap.getOrDefault(SCHEMA_NAME, "");
        String rowValue = rowMap.getOrDefault(schemaNameAlias, "").toString();
        queryDatabase = !rowValue.isEmpty();
        if (catalogs.contains(rowValue)) {
            rowMap.replace(schemaNameAlias, databaseName);
        } else {
            rowMap.clear();
        }
    }
    
    private void fillSchemasWithoutDataSource() {
        if (SCHEMA_WITHOUT_DATA_SOURCE.isEmpty()) {
            return;
        }
        Map<String, String> defaultRowData = getTheDefaultRowData();
        SCHEMA_WITHOUT_DATA_SOURCE.forEach(each -> {
            Map<String, Object> row = new HashMap<>(defaultRowData);
            row.replace(SCHEMA_NAME, each);
            getRows().addLast(row);
        });
        SCHEMA_WITHOUT_DATA_SOURCE.clear();
    }
    
    private Map<String, String> getTheDefaultRowData() {
        Map<String, String> result;
        Collection<ProjectionSegment> projections = sqlStatement.getProjections().getProjections();
        if (projections.stream().anyMatch(each -> each instanceof ShorthandProjectionSegment)) {
            result = Stream.of(CATALOG_NAME, SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME, SQL_PATH, DEFAULT_ENCRYPTION).collect(Collectors.toMap(each -> each, each -> ""));
        } else {
            result = projections.stream().map(each -> ((ColumnProjectionSegment) each).getColumn().getIdentifier()).map(IdentifierValue::getValue).collect(Collectors.toMap(each -> each, each -> ""));
        }
        return result;
    }
}
