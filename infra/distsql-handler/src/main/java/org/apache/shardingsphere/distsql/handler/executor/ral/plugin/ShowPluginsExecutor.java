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

package org.apache.shardingsphere.distsql.handler.executor.ral.plugin;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Show plugins executor.
 */
public final class ShowPluginsExecutor implements DistSQLQueryExecutor<ShowPluginsStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowPluginsStatement sqlStatement) {
        Optional<ShowPluginsResultRowBuilder> rowBuilder = TypedSPILoader.findService(ShowPluginsResultRowBuilder.class, sqlStatement.getType());
        return rowBuilder.isPresent() && DatabaseSupportedTypedSPI.class.isAssignableFrom(rowBuilder.get().getPluginClass())
                ? Arrays.asList("type", "type_aliases", "supported_database_types", "description")
                : Arrays.asList("type", "type_aliases", "description");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowPluginsStatement sqlStatement, final ContextManager contextManager) {
        return TypedSPILoader.findService(ShowPluginsResultRowBuilder.class, sqlStatement.getType()).map(optional -> optional.generateRows(sqlStatement)).orElse(Collections.emptyList());
    }
    
    @Override
    public Class<ShowPluginsStatement> getType() {
        return ShowPluginsStatement.class;
    }
}
