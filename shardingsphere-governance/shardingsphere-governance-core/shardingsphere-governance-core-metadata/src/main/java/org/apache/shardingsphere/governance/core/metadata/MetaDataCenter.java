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

package org.apache.shardingsphere.governance.core.metadata;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.repository.api.GovernanceRepository;
import org.apache.shardingsphere.governance.core.metadata.yaml.RuleSchemaMetaDataYamlSwapper;
import org.apache.shardingsphere.governance.core.metadata.yaml.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.eventbus.event.MetaDataEvent;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Optional;

/**
 * Meta data center.
 */
public final class MetaDataCenter {
    
    private final MetaDataCenterNode node;
    
    private final GovernanceRepository repository;
    
    public MetaDataCenter(final GovernanceRepository governanceRepository) {
        node = new MetaDataCenterNode();
        repository = governanceRepository;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Persist rule schema meta data to center repository.
     *
     * @param schemaName schema name
     * @param ruleSchemaMetaData rule schema meta data of the schema
     */
    public void persistMetaDataCenterNode(final String schemaName, final RuleSchemaMetaData ruleSchemaMetaData) {
        repository.persist(node.getMetaDataCenterNodeFullPath(schemaName), YamlEngine.marshal(new RuleSchemaMetaDataYamlSwapper().swapToYamlConfiguration(ruleSchemaMetaData)));
    }

    /**
     * Load rule schema meta data from center repository.
     *
     * @param schemaName schema name
     * @return rule schema meta data of the schema
     */
    public Optional<RuleSchemaMetaData> loadRuleSchemaMetaData(final String schemaName) {
        String path = repository.get(node.getMetaDataCenterNodeFullPath(schemaName));
        if (Strings.isNullOrEmpty(path)) {
            return Optional.empty();
        }
        return Optional.of(new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(path, YamlRuleSchemaMetaData.class)));
    }
    
    /**
     * Persist meta data.
     * 
     * @param event Meta data event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataEvent event) {
        persistMetaDataCenterNode(event.getSchemaName(), event.getMetaData());
    }
}
