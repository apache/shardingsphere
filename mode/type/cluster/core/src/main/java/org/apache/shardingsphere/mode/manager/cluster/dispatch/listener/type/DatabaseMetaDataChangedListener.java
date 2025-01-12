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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.node.path.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.MetaDataChangedHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.RuleConfigurationChangedHandler;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Database meta data changed listener.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataChangedListener implements DataChangedEventListener {
    
    private final ContextManager contextManager;
    
    @Override
    public void onChange(final DataChangedEvent event) {
        Optional<String> databaseName = DatabaseMetaDataNodePath.findDatabaseName(event.getKey(), true);
        if (!databaseName.isPresent()) {
            return;
        }
        OrderedServicesCache.clearCache();
        if (new MetaDataChangedHandler(contextManager).handle(databaseName.get(), event)) {
            return;
        }
        try {
            new RuleConfigurationChangedHandler(contextManager).handle(databaseName.get(), event);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
}
