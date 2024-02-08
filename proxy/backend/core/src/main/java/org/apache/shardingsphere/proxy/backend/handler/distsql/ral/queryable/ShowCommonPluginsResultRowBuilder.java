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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.plugin.PluginMetaDataQueryResultRows;
import org.apache.shardingsphere.distsql.handler.exception.plugin.PluginNotFoundException;
import org.apache.shardingsphere.distsql.handler.executor.ral.plugin.ShowPluginsResultRowBuilder;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Arrays;
import java.util.Collection;

/**
 * Show common plugins result row builder.
 */
public final class ShowCommonPluginsResultRowBuilder implements ShowPluginsResultRowBuilder {
    
    @Override
    public Collection<LocalDataQueryResultRow> generateRows(final ShowPluginsStatement sqlStatement) {
        PluginMetaDataQueryResultRows pluginMetaDataQueryResultRows = new PluginMetaDataQueryResultRows(getPluginClass(sqlStatement));
        return pluginMetaDataQueryResultRows.getRows();
    }
    
    @SuppressWarnings("unchecked")
    private static Class<? extends TypedSPI> getPluginClass(final ShowPluginsStatement sqlStatement) {
        try {
            Class<?> result = Class.forName(sqlStatement.getPluginClass());
            ShardingSpherePreconditions.checkState(TypedSPI.class.isAssignableFrom(result), () -> new UnsupportedOperationException("The plugin class to be queried must extend TypedSPI"));
            return (Class<? extends TypedSPI>) result;
        } catch (final ClassNotFoundException ex) {
            throw new PluginNotFoundException(sqlStatement.getPluginClass());
        }
    }
    
    @Override
    public Collection<String> getColumnNames() {
        // TODO change to pluginMetaDataQueryResultRows.getColumnNames after adding SQL statement as param for this method
        return Arrays.asList("type", "type_aliases", "description");
    }
    
    @Override
    public String getType() {
        return "COMMON";
    }
}
