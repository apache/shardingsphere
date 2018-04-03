/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.config;

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingRuleRegistry {
    
    private static final ShardingRuleRegistry INSTANCE = new ShardingRuleRegistry();
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private ShardingRuleRegistry() {
        YamlShardingConfiguration yamlShardingConfig;
        try {
            yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(getClass().getResource("/conf/sharding-config.yaml").getFile()));
        } catch (IOException ex) {
            throw new ShardingJdbcException(ex);
        }
        dataSourceMap = yamlShardingConfig.getDataSources();
        shardingRule = yamlShardingConfig.getShardingRule(Collections.<String>emptyList());
    }
    
    /**
     * Get instance of sharding rule registry.
     *
     * @return instance of sharding rule registry
     */
    public static ShardingRuleRegistry getInstance() {
        return INSTANCE;
    }
}
