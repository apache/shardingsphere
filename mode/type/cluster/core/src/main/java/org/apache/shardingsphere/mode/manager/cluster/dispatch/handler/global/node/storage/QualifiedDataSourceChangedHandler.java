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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.storage;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceStateSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.node.storage.QualifiedDataSourceNodePath;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Qualified data source changed handler.
 */
public final class QualifiedDataSourceChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new QualifiedDataSourceNodePath((String) null);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getValue())) {
            return;
        }
        Optional<QualifiedDataSource> qualifiedDataSource = NodePathSearcher.find(event.getKey(), QualifiedDataSourceNodePath.createQualifiedDataSourceSearchCriteria()).map(QualifiedDataSource::new);
        if (!qualifiedDataSource.isPresent()) {
            return;
        }
        QualifiedDataSourceState state = new YamlQualifiedDataSourceStateSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlQualifiedDataSourceState.class));
        handleQualifiedDataSourceStateChanged(contextManager.getMetaDataContexts().getMetaData(), qualifiedDataSource.get(), state);
    }
    
    private void handleQualifiedDataSourceStateChanged(final ShardingSphereMetaData metaData, final QualifiedDataSource qualifiedDataSource, final QualifiedDataSourceState state) {
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(qualifiedDataSource.getDatabaseName()), () -> new UnknownDatabaseException(qualifiedDataSource.getDatabaseName()));
        metaData.getDatabase(qualifiedDataSource.getDatabaseName()).getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class)
                .forEach(each -> each.updateStatus(qualifiedDataSource, state.getState()));
    }
}
