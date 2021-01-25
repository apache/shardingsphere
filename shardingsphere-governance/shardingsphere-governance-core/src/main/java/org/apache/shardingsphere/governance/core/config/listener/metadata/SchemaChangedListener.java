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

package org.apache.shardingsphere.governance.core.config.listener.metadata;

import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.yaml.config.schema.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.Optional;

/**
 * Schema changed listener.
 */
public final class SchemaChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    public SchemaChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, new ConfigCenterNode().getAllSchemaPaths(schemaNames));
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        String schemaName = new ConfigCenterNode().getSchemaName(event.getKey());
        return Optional.of(new SchemaChangedEvent(schemaName, new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlSchema.class))));
    }
}
