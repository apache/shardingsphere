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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Optional;

/**
 * Rule altered detector factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleAlteredDetectorFactory {
    
    static {
        ShardingSphereServiceLoader.register(RuleAlteredDetector.class);
    }
    
    /**
     * Find instance of rule altered detector.
     * 
     * @param ruleConfig rule configuration
     * @return found instance
     */
    public static Optional<RuleAlteredDetector> findInstance(final RuleConfiguration ruleConfig) {
        return ShardingSphereServiceLoader.getServiceInstances(RuleAlteredDetector.class).stream().filter(each -> each.getRuleConfigClassName().equals(ruleConfig.getClass().getName())).findFirst();
    }
    
    /**
     * Find instance of rule altered detector.
     * 
     * @param yamlRuleConfig YAML rule configuration
     * @return found instance
     */
    public static Optional<RuleAlteredDetector> findInstance(final YamlRuleConfiguration yamlRuleConfig) {
        return ShardingSphereServiceLoader.getServiceInstances(RuleAlteredDetector.class).stream()
                .filter(each -> each.getYamlRuleConfigClassName().equals(yamlRuleConfig.getClass().getName())).findFirst();
    }
}
