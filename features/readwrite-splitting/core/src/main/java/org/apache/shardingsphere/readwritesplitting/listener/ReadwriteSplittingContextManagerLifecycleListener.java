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

package org.apache.shardingsphere.readwritesplitting.listener;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListenerModeRequired;

import java.util.Map;

/**
 * Readwrite splitting context manager lifecycle listener.
 */
@ContextManagerLifecycleListenerModeRequired("Cluster")
public class ReadwriteSplittingContextManagerLifecycleListener implements ContextManagerLifecycleListener {
    
    @Override
    public void onInitialized(final ContextManager contextManager) {
        Map<String, QualifiedDataSourceState> qualifiedDataSourceStateMap = contextManager.getPersistServiceFacade().getQualifiedDataSourceStateService().load();
        qualifiedDataSourceStateMap.forEach((key, value) -> updateQualifiedDataSourceState(contextManager.getMetaDataContexts().getMetaData(), new QualifiedDataSource(key), value));
    }
    
    private void updateQualifiedDataSourceState(final ShardingSphereMetaData metaData, final QualifiedDataSource qualifiedDataSource, final QualifiedDataSourceState state) {
        metaData.getAllDatabases().forEach(each -> {
            if (each.getName().equals(qualifiedDataSource.getDatabaseName())) {
                each.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class).forEach(attribute -> attribute.updateStatus(qualifiedDataSource, state.getState()));
            }
        });
    }
    
    @Override
    public void onDestroyed(final ContextManager contextManager) {
    }
}
