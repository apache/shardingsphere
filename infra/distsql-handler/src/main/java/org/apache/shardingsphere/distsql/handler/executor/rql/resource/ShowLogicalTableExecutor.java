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
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.rql.resource.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show logical table executor.
 */
@Setter
public final class ShowLogicalTableExecutor implements DistSQLQueryExecutor<ShowLogicalTablesStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ShowLogicalTablesStatement sqlStatement) {
        return Collections.singleton("table_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowLogicalTablesStatement sqlStatement, final ContextManager contextManager) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        String schemaName = dialectDatabaseMetaData.getDefaultSchema().orElse(database.getName());
        if (null == database.getSchema(schemaName)) {
            return Collections.emptyList();
        }
        Collection<String> tables = database.getSchema(schemaName).getAllTableNames();
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = RegexUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            tables = tables.stream().filter(each -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(each).matches()).collect(Collectors.toList());
        }
        return tables.stream().map(LocalDataQueryResultRow::new).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowLogicalTablesStatement> getType() {
        return ShowLogicalTablesStatement.class;
    }
}
