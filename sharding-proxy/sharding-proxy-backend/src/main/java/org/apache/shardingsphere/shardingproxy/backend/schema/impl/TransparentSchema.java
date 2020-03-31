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

package org.apache.shardingsphere.shardingproxy.backend.schema.impl;

import lombok.Getter;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;

import java.sql.SQLException;
import java.util.Map;

/**
 * Transparent schema.
 */
@Getter
public final class TransparentSchema extends LogicSchema {
    
    private final ShardingRule shardingRule;
    
    public TransparentSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources) throws SQLException {
        super(name, dataSources);
        // TODO we should remove it after none-sharding parsingEngine completed.
        shardingRule = new ShardingRule(new ShardingRuleConfiguration(), getDataSources().keySet());
    }
    
    @Override
    public ShardingSphereMetaData getMetaData() {
        return getPhysicalMetaData();
    }
}
