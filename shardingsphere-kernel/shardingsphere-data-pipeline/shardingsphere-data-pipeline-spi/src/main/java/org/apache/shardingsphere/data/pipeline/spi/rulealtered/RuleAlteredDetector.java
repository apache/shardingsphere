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

package org.apache.shardingsphere.data.pipeline.spi.rulealtered;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;

import java.util.Optional;

/**
 * Rule altered detector, SPI interface.
 */
public interface RuleAlteredDetector {
    
    /**
     * Get YAML rule configuration class name.
     *
     * @return class name
     */
    String getYamlRuleConfigClassName();
    
    /**
     * Get rule configuration class name.
     *
     * @return class name
     */
    String getRuleConfigClassName();
    
    /**
     * Is rule altered.
     *
     * @param sourceRuleConfig source YAML rule configuration, may be null
     * @param targetRuleConfig target YAML rule configuration, may be null
     * @return whether is rule altered or not
     */
    boolean isRuleAltered(YamlRuleConfiguration sourceRuleConfig, YamlRuleConfiguration targetRuleConfig);
    
    /**
     * Get on rule altered action configuration.
     *
     * @param ruleConfig rule configuration, may be null
     * @return optional configuration
     */
    Optional<OnRuleAlteredActionConfiguration> getOnRuleAlteredActionConfig(RuleConfiguration ruleConfig);
}
