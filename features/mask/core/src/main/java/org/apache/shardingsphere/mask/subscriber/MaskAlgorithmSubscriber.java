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

package org.apache.shardingsphere.mask.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleChangedSubscriber;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.event.algorithm.AlterMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.algorithm.DeleteMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Map;

/**
 * Mask algorithm subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class MaskAlgorithmSubscriber implements RuleChangedSubscriber {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with alter algorithm.
     *
     * @param event alter algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterMaskAlgorithmEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        MaskRuleConfiguration config = (MaskRuleConfiguration) database.getRuleMetaData().getSingleRule(MaskRule.class).getConfiguration();
        config.getMaskAlgorithms().put(event.getAlgorithmName(), swapToAlgorithmConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion())));
    }
    
    /**
     * Renew with delete algorithm.
     *
     * @param event delete algorithm event
     */
    @Subscribe
    public synchronized void renew(final DeleteMaskAlgorithmEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        MaskRuleConfiguration config = (MaskRuleConfiguration) database.getRuleMetaData().getSingleRule(MaskRule.class).getConfiguration();
        config.getMaskAlgorithms().remove(event.getAlgorithmName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
