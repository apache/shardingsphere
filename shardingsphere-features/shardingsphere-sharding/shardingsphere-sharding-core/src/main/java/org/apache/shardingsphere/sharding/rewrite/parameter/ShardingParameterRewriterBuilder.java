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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
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

/**
 * Parameter rewriter builder for sharding.
 */
@RequiredArgsConstructor
public final class ShardingParameterRewriterBuilder implements ParameterRewriterBuilder {
    
    private final ShardingRule shardingRule;
    
    private final RouteContext routeContext;
    
    private final ShardingSphereSchema schema;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        addParameterRewriter(result, new ShardingGeneratedKeyInsertValueParameterRewriter());
        addParameterRewriter(result, new ShardingPaginationParameterRewriter());
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private void addParameterRewriter(final Collection<ParameterRewriter> parameterRewriters, final ParameterRewriter toBeAddedParameterRewriter) {
        if (toBeAddedParameterRewriter instanceof SchemaMetaDataAware) {
            ((SchemaMetaDataAware) toBeAddedParameterRewriter).setSchema(schema);
        }
        if (toBeAddedParameterRewriter instanceof ShardingRuleAware) {
            ((ShardingRuleAware) toBeAddedParameterRewriter).setShardingRule(shardingRule);
        }
        if (toBeAddedParameterRewriter instanceof RouteContextAware) {
            ((RouteContextAware) toBeAddedParameterRewriter).setRouteContext(routeContext);
        }
        if (toBeAddedParameterRewriter.isNeedRewrite(sqlStatementContext)) {
            parameterRewriters.add(toBeAddedParameterRewriter);
        }
    }
}
