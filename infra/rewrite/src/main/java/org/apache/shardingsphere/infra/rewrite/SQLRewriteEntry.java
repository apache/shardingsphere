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

package org.apache.shardingsphere.infra.rewrite;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.engine.GenericSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.RouteSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL rewrite entry.
 */
@HighFrequencyInvocation
public final class SQLRewriteEntry {
    
    private final ShardingSphereDatabase database;
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRewriteContextDecorator> decorators;
    
    public SQLRewriteEntry(final ShardingSphereDatabase database, final RuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        this.database = database;
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
        decorators = OrderedSPILoader.getServices(SQLRewriteContextDecorator.class, database.getRuleMetaData().getRules());
    }
    
    /**
     * Rewrite.
     *
     * @param queryContext query context
     * @param routeContext route context
     * @return route unit and SQL rewrite result map
     */
    public SQLRewriteResult rewrite(final QueryContext queryContext, final RouteContext routeContext) {
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(queryContext, routeContext);
        SQLTranslatorRule rule = globalRuleMetaData.getSingleRule(SQLTranslatorRule.class);
        return routeContext.getRouteUnits().isEmpty()
                ? new GenericSQLRewriteEngine(rule, database, globalRuleMetaData).rewrite(sqlRewriteContext, queryContext)
                : new RouteSQLRewriteEngine(rule, database, globalRuleMetaData).rewrite(sqlRewriteContext, routeContext, queryContext);
    }
    
    private SQLRewriteContext createSQLRewriteContext(final QueryContext queryContext, final RouteContext routeContext) {
        HintValueContext hintValueContext = queryContext.getHintValueContext();
        SQLRewriteContext result = new SQLRewriteContext(database, queryContext);
        decorate(result, routeContext, hintValueContext);
        result.generateSQLTokens();
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void decorate(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final HintValueContext hintValueContext) {
        if (hintValueContext.isSkipSQLRewrite()) {
            return;
        }
        for (Entry<ShardingSphereRule, SQLRewriteContextDecorator> entry : decorators.entrySet()) {
            entry.getValue().decorate(entry.getKey(), props, sqlRewriteContext, routeContext);
        }
    }
}
