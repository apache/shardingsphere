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

package org.apache.shardingsphere.infra.rule.builder.single;

import org.apache.shardingsphere.infra.config.single.SingleTableRuleConfiguration;
import org.apache.shardingsphere.infra.constant.SingleTableOrder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.level.FeatureRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.SchemaRuleBuilder;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Single table rule builder.
 */
public final class SingleTableRuleBuilder implements FeatureRuleBuilder, SchemaRuleBuilder<SingleTableRuleConfiguration> {
    
    @Override
    public SingleTableRule build(final String schemaName, final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType, 
                                 final SingleTableRuleConfiguration config, final Collection<ShardingSphereRule> rules) {
        return new SingleTableRule(databaseType, dataSourceMap, rules);
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getTypeClass() {
        return SingleTableRuleConfiguration.class;
    }
}
