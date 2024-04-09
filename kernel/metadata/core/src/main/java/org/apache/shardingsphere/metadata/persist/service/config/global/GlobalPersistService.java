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

package org.apache.shardingsphere.metadata.persist.service.config.global;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;

import java.util.Collection;

/**
 * Global persist service.
 * 
 * @param <T> type of configuration
 */
public interface GlobalPersistService<T> {
    
    /**
     * Persist configurations.
     *
     * @param globalRuleConfigs configurations
     */
    void persist(T globalRuleConfigs);
    
    /**
     * Persist configurations.
     *
     * @param globalRuleConfigs configurations
     * @return meta data versions
     */
    Collection<MetaDataVersion> persistConfig(T globalRuleConfigs);
    
    /**
     * Load configurations.
     *
     * @return configurations
     */
    T load();
    
    /**
     * Load single rule configuration.
     *
     * @param ruleName rule name
     * @return single rule configuration
     */
    default RuleConfiguration load(final String ruleName) {
        return null;
    }
}
