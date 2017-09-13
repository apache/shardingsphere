/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.yaml.internel;

import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlConfig {
    
    private Map<String, DataSource> dataSources = new HashMap<>();
    
    private String defaultDataSourceName;
    
    private Map<String, YamlTableRuleConfig> tables = new HashMap<>();
    
    private List<String> bindingTables = new ArrayList<>();
    
    private YamlShardingStrategyConfig defaultDatabaseStrategy;
    
    private YamlShardingStrategyConfig defaultTableStrategy;
    
    private String defaultKeyGeneratorClass;
    
    private Properties props = new Properties();
}
