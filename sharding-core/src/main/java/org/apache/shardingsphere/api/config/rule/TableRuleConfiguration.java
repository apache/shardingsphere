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

package org.apache.shardingsphere.api.config.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.strategy.ShardingStrategyConfiguration;

/**
 * Table rule configuration.
 * 
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class TableRuleConfiguration implements RuleConfiguration {
    
    private final String logicTable;
    
    private final String actualDataNodes;
    
    private ShardingStrategyConfiguration databaseShardingStrategyConfig;
    
    private ShardingStrategyConfiguration tableShardingStrategyConfig;
    
    private KeyGeneratorConfiguration keyGeneratorConfig;
    
    private EncryptorConfiguration encryptorConfig;
    
    private String logicIndex;
    
    public TableRuleConfiguration(final String logicTable) {
        this(logicTable, null);
    }
}
