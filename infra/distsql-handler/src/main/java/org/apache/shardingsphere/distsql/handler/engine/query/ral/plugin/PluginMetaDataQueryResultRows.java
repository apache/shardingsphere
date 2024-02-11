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

package org.apache.shardingsphere.distsql.handler.engine.query.ral.plugin;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Plugin meta data query result rows.
 */
public final class PluginMetaDataQueryResultRows {
    
    private final Collection<PluginMetaDataQueryResultRow> rows;
    
    public PluginMetaDataQueryResultRows(final Class<? extends TypedSPI> pluginClass) {
        rows = ShardingSphereServiceLoader.getServiceInstances(pluginClass).stream().map(PluginMetaDataQueryResultRow::new).collect(Collectors.toList());
    }
    
    /**
     * Get rows.
     * 
     * @return rows
     */
    public Collection<LocalDataQueryResultRow> getRows() {
        return rows.stream().map(PluginMetaDataQueryResultRow::toLocalDataQueryResultRow).collect(Collectors.toList());
    }
}
