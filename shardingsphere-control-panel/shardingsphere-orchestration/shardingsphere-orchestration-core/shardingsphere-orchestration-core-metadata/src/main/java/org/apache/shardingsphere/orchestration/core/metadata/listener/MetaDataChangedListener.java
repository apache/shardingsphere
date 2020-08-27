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

package org.apache.shardingsphere.orchestration.core.metadata.listener;

import org.apache.shardingsphere.orchestration.core.metadata.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.OrchestrationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.OrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.listener.PostOrchestrationRepositoryEventListener;
import org.apache.shardingsphere.orchestration.core.metadata.yaml.RuleSchemaMetaDataYamlSwapper;
import org.apache.shardingsphere.orchestration.core.metadata.MetaDataCenterNode;
import org.apache.shardingsphere.orchestration.core.metadata.yaml.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.Optional;

/**
 * Meta data changed listener.
 */
public final class MetaDataChangedListener extends PostOrchestrationRepositoryEventListener {
    
    private final Collection<String> schemaNames;
    
    public MetaDataChangedListener(final OrchestrationRepository orchestrationRepository, final Collection<String> schemaNames) {
        super(orchestrationRepository, new MetaDataCenterNode().getAllSchemaMetadataPaths(schemaNames));
        this.schemaNames = schemaNames;
    }
    
    @Override
    protected Optional<OrchestrationEvent> createOrchestrationEvent(final DataChangedEvent event) {
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(event.getValue(), YamlRuleSchemaMetaData.class));
        return Optional.of(new MetaDataChangedEvent(schemaNames, ruleSchemaMetaData));
    }
}
