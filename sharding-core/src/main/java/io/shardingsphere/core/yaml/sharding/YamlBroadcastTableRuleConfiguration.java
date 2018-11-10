/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.BroadcastTableRuleConfiguration;
import io.shardingsphere.core.keygen.KeyGeneratorFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Yaml broadcast table rule configuration.
 *
 * @author maxiaoguang
 */
@NoArgsConstructor
@Getter
@Setter
public class YamlBroadcastTableRuleConfiguration {
    
    private String logicTable;
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorClassName;
    
    public YamlBroadcastTableRuleConfiguration(final BroadcastTableRuleConfiguration broadcastTableRuleConfiguration) {
        logicTable = broadcastTableRuleConfiguration.getLogicTable();
        keyGeneratorColumnName = broadcastTableRuleConfiguration.getKeyGeneratorColumnName();
        keyGeneratorClassName = null == broadcastTableRuleConfiguration.getKeyGenerator()
                ? null : broadcastTableRuleConfiguration.getKeyGenerator().getClass().getName();
    }
    
    /**
     * Build broadcast table rule configuration.
     *
     * @return broadcast table rule configuration
     */
    public BroadcastTableRuleConfiguration build() {
        Preconditions.checkNotNull(logicTable, "Logic table cannot be null.");
        BroadcastTableRuleConfiguration result = new BroadcastTableRuleConfiguration();
        result.setLogicTable(logicTable);
        if (!Strings.isNullOrEmpty(keyGeneratorClassName)) {
            result.setKeyGenerator(KeyGeneratorFactory.newInstance(keyGeneratorClassName));
        }
        result.setKeyGeneratorColumnName(keyGeneratorColumnName);
        return result;
    }
}
