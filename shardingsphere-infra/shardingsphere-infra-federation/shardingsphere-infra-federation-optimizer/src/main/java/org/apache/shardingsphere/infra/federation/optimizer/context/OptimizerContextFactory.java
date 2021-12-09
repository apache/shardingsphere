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

package org.apache.shardingsphere.infra.federation.optimizer.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.util.Map;

/**
 * Optimizer context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerContextFactory {
    
    /**
     * Create optimize context.
     *
     * @param metaDataMap meta data map
     * @param globalRuleMetaData global rule meta data
     * @return created optimizer context
     */
    public static OptimizerContext create(final Map<String, ShardingSphereMetaData> metaDataMap, final ShardingSphereRuleMetaData globalRuleMetaData) {
        FederationMetaData federationMetaData = new FederationMetaData(metaDataMap);
        Map<String, OptimizerParserContext> parserContexts = OptimizerParserContextFactory.create(metaDataMap);
        Map<String, OptimizerPlannerContext> plannerContexts = OptimizerPlannerContextFactory.create(federationMetaData);
        SQLParserRule sqlParserRule = globalRuleMetaData.findSingleRule(SQLParserRule.class).orElse(null);
        return new OptimizerContext(sqlParserRule, federationMetaData, metaDataMap, parserContexts, plannerContexts);
    }
}
