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

package org.apache.shardingsphere.mode.spi;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Rule item configuration changed processor.
 *
 * @param <T> type of rule configuration
 * @param <I> type of rule item configuration
 */
@SingletonSPI
public interface RuleItemConfigurationChangedProcessor<T extends RuleConfiguration, I> extends TypedSPI {
    
    /**
     * Swap rule item configuration.
     * 
     * @param event alter rule item event
     * @param yamlContent YAML content
     * @return rule item configuration
     */
    I swapRuleItemConfiguration(AlterRuleItemEvent event, String yamlContent);
    
    /**
     * Find rule configuration.
     * 
     * @param database database
     * @return found rule configuration
     */
    T findRuleConfiguration(ShardingSphereDatabase database);
    
    /**
     * Change rule item configuration.
     * 
     * @param event alter rule item event
     * @param currentRuleConfig current rule configuration
     * @param toBeChangedItemConfig to be changed item configuration
     */
    void changeRuleItemConfiguration(AlterRuleItemEvent event, T currentRuleConfig, I toBeChangedItemConfig);
    
    /**
     * Drop rule item configuration.
     * 
     * @param event drop rule item event
     * @param currentRuleConfig current rule configuration
     */
    void dropRuleItemConfiguration(DropRuleItemEvent event, T currentRuleConfig);
}
