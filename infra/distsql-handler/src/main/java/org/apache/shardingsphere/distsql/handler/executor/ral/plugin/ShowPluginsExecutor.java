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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.PluginNotFoundException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.plugin.PluginTypeAndClassMapper;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Show plugins executor.
 */
public final class ShowPluginsExecutor implements DistSQLQueryExecutor<ShowPluginsStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowPluginsStatement sqlStatement) {
        return getColumnNames(getPluginClass(sqlStatement));
    }
    
    private List<String> getColumnNames(final Class<? extends TypedSPI> pluginClass) {
        return DatabaseSupportedTypedSPI.class.isAssignableFrom(pluginClass)
                ? Arrays.asList("type", "type_aliases", "supported_database_types", "description")
                : Arrays.asList("type", "type_aliases", "description");
    }
    
    private Class<? extends TypedSPI> getPluginClass(final ShowPluginsStatement sqlStatement) {
        return sqlStatement.getPluginClass().isPresent()
                ? getPluginClass(sqlStatement.getPluginClass().get())
                : TypedSPILoader.getService(PluginTypeAndClassMapper.class, sqlStatement.getType()).getPluginClass();
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends TypedSPI> getPluginClass(final String pluginClass) {
        try {
            Class<?> result = Class.forName(pluginClass);
            ShardingSpherePreconditions.checkState(TypedSPI.class.isAssignableFrom(result), () -> new UnsupportedOperationException("The plugin class to be queried must extend TypedSPI."));
            return (Class<? extends TypedSPI>) result;
        } catch (final ClassNotFoundException ignored) {
            throw new PluginNotFoundException(pluginClass);
        }
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowPluginsStatement sqlStatement, final ContextManager contextManager) {
        return ShardingSphereServiceLoader.getServiceInstances(getPluginClass(sqlStatement)).stream()
                .map(each -> new PluginMetaDataQueryResultRow(each).toLocalDataQueryResultRow()).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowPluginsStatement> getType() {
        return ShowPluginsStatement.class;
    }
}
