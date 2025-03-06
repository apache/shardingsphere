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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type;

import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.StorageNodeChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.StorageUnitChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.ViewChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.SchemaChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.TableChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.RuleConfigurationChangedHandler;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.schema.TableMetadataNodePath;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Database meta data changed listener.
 */
public final class DatabaseMetaDataChangedListener implements DataChangedEventListener {
    
    private final Collection<DatabaseChangedHandler> handlers;
    
    public DatabaseMetaDataChangedListener(final ContextManager contextManager) {
        handlers = Arrays.asList(
                new SchemaChangedHandler(contextManager),
                new TableChangedHandler(contextManager),
                new ViewChangedHandler(contextManager),
                new StorageUnitChangedHandler(contextManager),
                new StorageNodeChangedHandler(contextManager),
                new RuleConfigurationChangedHandler(contextManager));
    }
    
    @Override
    public void onChange(final DataChangedEvent event) {
        Optional<String> databaseName = NodePathSearcher.find(event.getKey(), TableMetadataNodePath.createDatabaseSearchCriteria());
        if (!databaseName.isPresent()) {
            return;
        }
        OrderedServicesCache.clearCache();
        for (DatabaseChangedHandler each : handlers) {
            if (each.isSubscribed(databaseName.get(), event.getKey())) {
                try {
                    each.handle(databaseName.get(), event);
                    break;
                } catch (final SQLException ex) {
                    throw new SQLWrapperException(ex);
                }
            }
        }
    }
}
