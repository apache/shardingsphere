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

package org.apache.shardingsphere.sqlfederation.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MetaDataHeldRule;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.compiler.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.OptimizerContextFactory;
import org.apache.shardingsphere.sqlfederation.compiler.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.parser.dialect.OptimizerSQLPropertiesBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.planner.OptimizerPlannerContextFactory;

import java.util.Map;

/**
 * SQL federation rule.
 */
@Getter
public final class SQLFederationRule implements GlobalRule, MetaDataHeldRule {
    
    private final SQLFederationRuleConfiguration configuration;
    
    private final OptimizerContext optimizerContext;
    
    public SQLFederationRule(final SQLFederationRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        configuration = ruleConfig;
        optimizerContext = OptimizerContextFactory.create(databases, props);
    }
    
    @Override
    public void alterDatabase(final ShardingSphereDatabase database) {
        DatabaseType databaseType = database.getProtocolType();
        OptimizerParserContext parserContext = new OptimizerParserContext(databaseType, new OptimizerSQLPropertiesBuilder(databaseType).build());
        optimizerContext.putParserContext(database.getName(), parserContext);
        OptimizerPlannerContext plannerContext = OptimizerPlannerContextFactory.create(database, parserContext, optimizerContext.getSqlParserRule());
        optimizerContext.putPlannerContext(database.getName(), plannerContext);
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        optimizerContext.removeParserContext(databaseName);
        optimizerContext.removePlannerContext(databaseName);
    }
    
    @Override
    public String getType() {
        return SQLFederationRule.class.getSimpleName();
    }
}
