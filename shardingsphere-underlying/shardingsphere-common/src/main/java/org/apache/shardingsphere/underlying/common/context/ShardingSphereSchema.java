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

package org.apache.shardingsphere.underlying.common.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRulesBuilder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@RequiredArgsConstructor
public final class ShardingSphereSchema {
    
    private final DatabaseType databaseType;
    
    private final Collection<RuleConfiguration> configurations = new LinkedList<>();
    
    private final Collection<ShardingSphereRule> rules;
    
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();
    
    private ShardingSphereMetaData metaData;
    
    public ShardingSphereSchema(final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, 
                                final Map<String, DataSource> dataSourceMap, final ShardingSphereMetaData shardingSphereMetaData) {
        this.databaseType = databaseType;
        this.configurations.addAll(configurations);
        rules = ShardingSphereRulesBuilder.build(configurations, dataSourceMap.keySet());
        this.dataSources.putAll(dataSourceMap);
        metaData = shardingSphereMetaData;
    }
}
