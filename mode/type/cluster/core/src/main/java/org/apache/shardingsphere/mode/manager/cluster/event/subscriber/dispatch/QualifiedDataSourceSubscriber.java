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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.event.dispatch.state.storage.QualifiedDataSourceStateEvent;

/**
 * Qualified data source subscriber.
 */
@RequiredArgsConstructor
public class QualifiedDataSourceSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew disabled data source names.
     *
     * @param event qualified data source state event
     */
    @Subscribe
    public synchronized void renew(final QualifiedDataSourceStateEvent event) {
        QualifiedDataSource qualifiedDataSource = event.getQualifiedDataSource();
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(qualifiedDataSource.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(qualifiedDataSource.getDatabaseName());
        for (StaticDataSourceRuleAttribute each : database.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class)) {
            each.updateStatus(qualifiedDataSource, event.getStatus().getState());
        }
    }
}
