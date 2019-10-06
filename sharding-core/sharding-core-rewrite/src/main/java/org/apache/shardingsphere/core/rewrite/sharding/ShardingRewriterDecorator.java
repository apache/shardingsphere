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

package org.apache.shardingsphere.core.rewrite.sharding;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rewrite.BasicRewriter;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.sharding.ShardingParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.sql.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Rewriter decorator for sharding.
 * 
 * @author zhangliang
 */
public final class ShardingRewriterDecorator {
    
    /**
     * Decorate rewriter.
     * 
     * @param rewriter rewriter to be decorated
     * @param parameters SQL parameters
     * @param shardingRule sharding rule
     * @param tableMetas table metas
     * @param sqlRouteResult SQL route result
     */
    public void decorate(final BasicRewriter rewriter, final List<Object> parameters, final ShardingRule shardingRule, final TableMetas tableMetas, final SQLRouteResult sqlRouteResult) {
        ShardingParameterBuilderFactory.build(rewriter.getParameterBuilder(), shardingRule, tableMetas, sqlRouteResult, parameters);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new ShardingTokenGenerateBuilder(shardingRule, sqlRouteResult).getSQLTokenGenerators());
        rewriter.addSQLTokens(sqlTokenGenerators.generateSQLTokens(rewriter.getSqlStatementContext(), 
                parameters, tableMetas, rewriter.getSqlBuilder().getSqlTokens(), sqlRouteResult.getRoutingResult().isSingleRouting()));
    }
}
