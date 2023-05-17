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

package org.apache.shardingsphere.sharding.rewrite.parameter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingGeneratedKeyInsertValueParameterRewriter;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingPaginationParameterRewriter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Parameter rewriter builder for sharding.
 */
@RequiredArgsConstructor
public final class ShardingParameterRewriterBuilder implements ParameterRewriterBuilder {
    
    private final ShardingRule shardingRule;
    
    private final RouteContext routeContext;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        addParameterRewriter(result, new ShardingGeneratedKeyInsertValueParameterRewriter());
        addParameterRewriter(result, new ShardingPaginationParameterRewriter());
        return result;
    }
    
    private void addParameterRewriter(final Collection<ParameterRewriter> paramRewriters, final ParameterRewriter toBeAddedParamRewriter) {
        if (toBeAddedParamRewriter instanceof SchemaMetaDataAware) {
            ((SchemaMetaDataAware) toBeAddedParamRewriter).setSchemas(schemas);
        }
        if (toBeAddedParamRewriter instanceof ShardingRuleAware) {
            ((ShardingRuleAware) toBeAddedParamRewriter).setShardingRule(shardingRule);
        }
        if (toBeAddedParamRewriter instanceof RouteContextAware) {
            ((RouteContextAware) toBeAddedParamRewriter).setRouteContext(routeContext);
        }
        if (toBeAddedParamRewriter.isNeedRewrite(sqlStatementContext)) {
            paramRewriters.add(toBeAddedParamRewriter);
        }
    }
}
