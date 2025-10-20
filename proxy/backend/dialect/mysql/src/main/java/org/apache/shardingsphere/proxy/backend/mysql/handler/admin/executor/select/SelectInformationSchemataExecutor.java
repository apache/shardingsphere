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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Select information schemata executor.
 */
public final class SelectInformationSchemataExecutor extends DatabaseMetaDataExecutor {
    
    public static final String SCHEMA_NAME = "SCHEMA_NAME";
    
    public static final String DEFAULT_CHARACTER_SET_NAME = "DEFAULT_CHARACTER_SET_NAME";
    
    public static final String DEFAULT_COLLATION_NAME = "DEFAULT_COLLATION_NAME";
    
    public static final String CATALOG_NAME = "CATALOG_NAME";
    
    public static final String SQL_PATH = "SQL_PATH";
    
    public static final String DEFAULT_ENCRYPTION = "DEFAULT_ENCRYPTION";
    
    private static final Collection<String> EMPTY_DATABASES = new LinkedHashSet<>();
    
    private final SelectStatement sqlStatement;
    
    private String schemaNameAlias = SCHEMA_NAME;
    
    private boolean queryDatabase;
    
    public SelectInformationSchemataExecutor(final SelectStatement sqlStatement, final String sql, final List<Object> parameters) {
        super(sql, parameters);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    protected Collection<ShardingSphereDatabase> getDatabases(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        AuthorityChecker authorityChecker = new AuthorityChecker(metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class), connectionSession.getConnectionContext().getGrantee());
        Collection<ShardingSphereDatabase> databases = metaData.getAllDatabases().stream().filter(each -> authorityChecker.isAuthorized(each.getName())).collect(Collectors.toList());
        EMPTY_DATABASES.addAll(databases.stream().filter(each -> !each.containsDataSource()).map(ShardingSphereDatabase::getName).collect(Collectors.toSet()));
        Collection<ShardingSphereDatabase> result = databases.stream().filter(ShardingSphereDatabase::containsDataSource).collect(Collectors.toList());
        if (!EMPTY_DATABASES.isEmpty()) {
            fillDatabasesWithoutDataSource(getDefaultRowData());
        }
        return result;
    }
    
    private void fillDatabasesWithoutDataSource(final Map<String, String> defaultRowData) {
        for (String each : EMPTY_DATABASES) {
            Map<String, Object> row = new LinkedHashMap<>(defaultRowData);
            row.replace(schemaNameAlias, each);
            getRows().add(row);
        }
        EMPTY_DATABASES.clear();
    }
    
    private Map<String, String> getDefaultRowData() {
        Collection<ProjectionSegment> projections = sqlStatement.getProjections().getProjections();
        return projections.stream().anyMatch(ShorthandProjectionSegment.class::isInstance)
                ? getDefaultRowsFromShorthandProjection()
                : getDefaultRowsFromColumnProjections(filterColumnProjections(projections));
    }
    
    private Map<String, String> getDefaultRowsFromShorthandProjection() {
        return Stream.of(CATALOG_NAME, SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME, SQL_PATH, DEFAULT_ENCRYPTION)
                .collect(Collectors.toMap(each -> each, each -> "", (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private List<ColumnProjectionSegment> filterColumnProjections(final Collection<ProjectionSegment> projections) {
        return projections.stream().filter(each -> each.getClass().isAssignableFrom(ColumnProjectionSegment.class)).map(ColumnProjectionSegment.class::cast).collect(Collectors.toList());
    }
    
    private Map<String, String> getDefaultRowsFromColumnProjections(final Collection<ColumnProjectionSegment> projections) {
        Map<String, String> result = new LinkedHashMap<>(projections.size(), 1F);
        for (ColumnProjectionSegment each : projections) {
            if (each.getAlias().isPresent()) {
                String alias = each.getAlias().get().getValue();
                if (each.getColumn().getIdentifier().getValue().equalsIgnoreCase(SCHEMA_NAME)) {
                    schemaNameAlias = alias;
                }
                result.put(alias, "");
                continue;
            }
            result.put(each.getColumn().getIdentifier().getValue().toUpperCase(), "");
        }
        return result;
    }
    
    @Override
    protected void preProcess(final ShardingSphereDatabase database, final Map<String, Object> rows, final Map<String, String> alias) throws SQLException {
        Optional<String> catalog = findCatalog(database.getResourceMetaData());
        schemaNameAlias = alias.getOrDefault(SCHEMA_NAME, alias.getOrDefault(schemaNameAlias, schemaNameAlias));
        String rowValue = rows.getOrDefault(schemaNameAlias, "").toString();
        queryDatabase = !rowValue.isEmpty();
        if (catalog.isPresent() && rowValue.equals(catalog.get())) {
            rows.replace(schemaNameAlias, database.getName());
        } else {
            rows.clear();
        }
    }
    
    private Optional<String> findCatalog(final ResourceMetaData resourceMetaData) throws SQLException {
        Optional<StorageUnit> storageUnit = resourceMetaData.getStorageUnits().values().stream().findFirst();
        if (!storageUnit.isPresent()) {
            return Optional.empty();
        }
        try (Connection connection = storageUnit.get().getDataSource().getConnection()) {
            return Optional.of(connection.getCatalog());
        }
    }
    
    @Override
    protected void postProcess() {
        removeDuplicatedRow();
    }
    
    private void removeDuplicatedRow() {
        if (queryDatabase) {
            Collection<Map<String, Object>> reservedRow = getRows().stream()
                    .collect(Collectors.groupingBy(each -> Optional.ofNullable(each.get(schemaNameAlias)), Collectors.toCollection(LinkedList::new)))
                    .values().stream().map(LinkedList::getFirst).collect(Collectors.toList());
            reservedRow.forEach(each -> getRows().removeIf(row -> !getRows().contains(each)));
        }
    }
}
