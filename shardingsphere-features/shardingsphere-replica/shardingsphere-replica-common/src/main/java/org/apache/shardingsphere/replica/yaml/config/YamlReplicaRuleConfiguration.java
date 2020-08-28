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

package org.apache.shardingsphere.replica.yaml.config;

import lombok.Getter;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaTableRuleConfiguration;

import java.util.Collections;
import java.util.Map;

/**
 * Replica rule configuration for YAML.
 */
@Getter
public final class YamlReplicaRuleConfiguration implements YamlRuleConfiguration {
    
    private Map<String, ReplicaTableRuleConfiguration[]> tables = Collections.emptyMap();
    
    /**
     * Set logic tables rules.
     *
     * @param tables tables
     */
    public void setTables(final Map<String, ReplicaTableRuleConfiguration[]> tables) {
        if (null == tables) {
            this.tables = Collections.emptyMap();
        } else {
            this.tables = tables;
        }
    }
    
    @Override
    public Class<ReplicaRuleConfiguration> getRuleConfigurationType() {
        return ReplicaRuleConfiguration.class;
    }
}
