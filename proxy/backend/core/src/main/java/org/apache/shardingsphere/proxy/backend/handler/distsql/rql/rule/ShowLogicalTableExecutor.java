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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowLogicalTablesStatement;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.regular.RegularUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show logical table executor.
 */
public final class ShowLogicalTableExecutor implements RQLExecutor<ShowLogicalTablesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowLogicalTablesStatement sqlStatement) {
        String schemaName = database.getName();
        if (database.getProtocolType() instanceof SchemaSupportedDatabaseType) {
            schemaName = ((SchemaSupportedDatabaseType) database.getProtocolType()).getDefaultSchema();
        }
        if (null == database.getSchema(schemaName)) {
            return Collections.emptyList();
        }
        Collection<String> tables = database.getSchema(schemaName).getAllTableNames();
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = SQLUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            tables = tables.stream().filter(each -> RegularUtils.matchesCaseInsensitive(pattern, each)).collect(Collectors.toList());
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        tables.forEach(each -> result.add(new LocalDataQueryResultRow(each)));
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singletonList("table_name");
    }
    
    @Override
    public String getType() {
        return ShowLogicalTablesStatement.class.getName();
    }
}
