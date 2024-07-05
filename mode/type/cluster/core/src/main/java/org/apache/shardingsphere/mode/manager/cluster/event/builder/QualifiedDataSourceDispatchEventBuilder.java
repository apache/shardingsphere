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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.state.storage.QualifiedDataSourceStateEvent;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.metadata.persist.node.QualifiedDataSourceNode;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceStateSwapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Qualified data source dispatch event builder.
 */
public final class QualifiedDataSourceDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(QualifiedDataSourceNode.getRootPath());
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<QualifiedDataSource> qualifiedDataSource = QualifiedDataSourceNode.extractQualifiedDataSource(event.getKey());
        if (qualifiedDataSource.isPresent()) {
            QualifiedDataSourceState state = new YamlQualifiedDataSourceStateSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlQualifiedDataSourceState.class));
            return Optional.of(new QualifiedDataSourceStateEvent(qualifiedDataSource.get(), state));
        }
        return Optional.empty();
    }
}
