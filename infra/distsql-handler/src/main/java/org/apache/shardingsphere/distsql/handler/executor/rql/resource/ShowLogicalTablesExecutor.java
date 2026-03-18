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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show logical tables executor.
 */
@Setter
public final class ShowLogicalTablesExecutor implements DistSQLQueryExecutor<ShowLogicalTablesStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ShowLogicalTablesStatement sqlStatement) {
        if (new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()) {
            return sqlStatement.isContainsFull() ? Arrays.asList("table_name", "table_type", "schema_name") : Arrays.asList("table_name", "schema_name");
        }
        return sqlStatement.isContainsFull() ? Arrays.asList("table_name", "table_type") : Collections.singleton("table_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowLogicalTablesStatement sqlStatement, final ContextManager contextManager) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        String schemaName = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().orElse(database.getName());
        if (null == database.getSchema(schemaName)) {
            return Collections.emptyList();
        }
        return getTables(schemaName, sqlStatement).stream().map(each -> getRow(schemaName, each, sqlStatement)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow getRow(final String schemaName, final ShardingSphereTable table, final ShowLogicalTablesStatement sqlStatement) {
        if (new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()) {
            return sqlStatement.isContainsFull() ? new LocalDataQueryResultRow(table.getName(), table.getType(), schemaName) : new LocalDataQueryResultRow(table.getName(), schemaName);
        }
        return sqlStatement.isContainsFull() ? new LocalDataQueryResultRow(table.getName(), table.getType()) : new LocalDataQueryResultRow(table.getName());
    }
    
    private Collection<ShardingSphereTable> getTables(final String schemaName, final ShowLogicalTablesStatement sqlStatement) {
        Collection<ShardingSphereTable> tables = database.getSchema(schemaName).getAllTables();
        Collection<ShardingSphereTable> filteredTables = filterByLike(tables, sqlStatement);
        return filteredTables.stream().sorted(Comparator.comparing(ShardingSphereTable::getName)).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereTable> filterByLike(final Collection<ShardingSphereTable> tables, final ShowLogicalTablesStatement sqlStatement) {
        Optional<Pattern> likePattern = getLikePattern(sqlStatement);
        return likePattern.isPresent() ? tables.stream().filter(each -> likePattern.get().matcher(each.getName()).matches()).collect(Collectors.toList()) : tables;
    }
    
    private Optional<Pattern> getLikePattern(final ShowLogicalTablesStatement sqlStatement) {
        return sqlStatement.getLikePattern().map(optional -> Pattern.compile(RegexUtils.convertLikePatternToRegex(optional), Pattern.CASE_INSENSITIVE));
    }
    
    @Override
    public Class<ShowLogicalTablesStatement> getType() {
        return ShowLogicalTablesStatement.class;
    }
}
