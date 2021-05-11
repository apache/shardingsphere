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

package org.apache.shardingsphere.readwritesplitting.common.rule.biulder;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.level.FeatureRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.SchemaRuleBuilder;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.common.rule.ReadwriteSplittingRule;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Algorithm provided readwrite-splitting rule builder.
 */
public final class AlgorithmProvidedReadwriteSplittingRuleBuilder implements FeatureRuleBuilder, SchemaRuleBuilder<ReadwriteSplittingRule, AlgorithmProvidedReadwriteSplittingRuleConfiguration> {
    
    @Override
    public ReadwriteSplittingRule build(final String schemaName, final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType,
                                        final AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig) {
        return new ReadwriteSplittingRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER + 1;
    }
    
    @Override
    public Class<AlgorithmProvidedReadwriteSplittingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedReadwriteSplittingRuleConfiguration.class;
    }
}
