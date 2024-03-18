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

package org.apache.shardingsphere.sqlfederation.optimizer.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.OptimizerParserContextFactory;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerPlannerContextFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Optimizer context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerContextFactory {
    
    /**
     * Create optimize context.
     *
     * @param databases databases
     * @return created optimizer context
     */
    public static OptimizerContext create(final Map<String, ShardingSphereDatabase> databases) {
        Map<String, OptimizerParserContext> parserContexts = OptimizerParserContextFactory.create(databases);
        // TODO consider to use sqlParserRule in global rule
        SQLParserRule sqlParserRule = new SQLParserRuleBuilder().build(new DefaultSQLParserRuleConfigurationBuilder().build(), databases, new ConfigurationProperties(new Properties()));
        Map<String, OptimizerPlannerContext> plannerContexts = OptimizerPlannerContextFactory.create(databases, parserContexts, sqlParserRule);
        return new OptimizerContext(sqlParserRule, parserContexts, plannerContexts);
    }
}
