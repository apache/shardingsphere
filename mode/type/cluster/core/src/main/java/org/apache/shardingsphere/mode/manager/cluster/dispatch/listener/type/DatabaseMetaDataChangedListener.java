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

import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseLeafValueChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseNodeValueChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.StorageNodeChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource.StorageUnitChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.SchemaChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.TableChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.ViewChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.type.NamedRuleItemConfigurationChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.type.RuleTypeConfigurationChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.type.UniqueRuleItemConfigurationChangedHandler;
import org.apache.shardingsphere.mode.metadata.manager.ActiveVersionChecker;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Database meta data changed listener.
 */
public final class DatabaseMetaDataChangedListener implements DataChangedEventListener {
    
    private final ContextManager contextManager;
    
    private final Collection<DatabaseChangedHandler> handlers;
    
    public DatabaseMetaDataChangedListener(final ContextManager contextManager) {
        this.contextManager = contextManager;
        handlers = Arrays.asList(
                new SchemaChangedHandler(contextManager),
                new TableChangedHandler(contextManager),
                new ViewChangedHandler(contextManager),
                new StorageUnitChangedHandler(contextManager),
                new StorageNodeChangedHandler(contextManager),
                new NamedRuleItemConfigurationChangedHandler(contextManager),
                new UniqueRuleItemConfigurationChangedHandler(contextManager),
                new RuleTypeConfigurationChangedHandler(contextManager));
    }
    
    @Override
    public void onChange(final DataChangedEvent event) {
        Optional<String> databaseName = NodePathSearcher.find(event.getKey(), DatabaseMetaDataNodePath.createDatabaseSearchCriteria());
        if (!databaseName.isPresent()) {
            return;
        }
        OrderedServicesCache.clearCache();
        for (DatabaseChangedHandler each : handlers) {
            if (!isSubscribed(each, databaseName.get(), event)) {
                continue;
            }
            if ((DataChangedEvent.Type.ADDED == event.getType() || DataChangedEvent.Type.UPDATED == event.getType())
                    && !new ActiveVersionChecker(contextManager.getPersistServiceFacade().getRepository()).checkSame(event)) {
                return;
            }
            each.handle(databaseName.get(), event);
            return;
        }
    }
    
    private boolean isSubscribed(final DatabaseChangedHandler handler, final String databaseName, final DataChangedEvent event) {
        if (handler instanceof DatabaseLeafValueChangedHandler) {
            if (DataChangedEvent.Type.ADDED == event.getType() || DataChangedEvent.Type.UPDATED == event.getType()) {
                return new VersionNodePath(handler.getSubscribedNodePath(databaseName)).isActiveVersionPath(event.getKey());
            } else {
                return NodePathSearcher.isMatchedPath(event.getKey(), new NodePathSearchCriteria(handler.getSubscribedNodePath(databaseName), false, 1))
                        && !new VersionNodePath(handler.getSubscribedNodePath(databaseName)).isActiveVersionPath(event.getKey())
                        && !new VersionNodePath(handler.getSubscribedNodePath(databaseName)).isVersionsPath(event.getKey());
            }
        }
        if (handler instanceof DatabaseNodeValueChangedHandler) {
            return NodePathSearcher.isMatchedPath(event.getKey(), new NodePathSearchCriteria(handler.getSubscribedNodePath(databaseName), false, 1));
        }
        return false;
    }
}
