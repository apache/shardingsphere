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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.SQLRewriteBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteBuilderDecorator;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.sharding.ShardingParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * SQL rewrite builder decorator for sharding.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingRewriteBuilderDecorator implements SQLRewriteBuilderDecorator {
    
    private final ShardingRule shardingRule;
    
    private final SQLRouteResult sqlRouteResult;
    
    @Override
    public void decorate(final SQLRewriteBuilder sqlRewriteBuilder, final TableMetas tableMetas, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        ShardingParameterBuilderFactory.build(sqlRewriteBuilder.getParameterBuilder(), shardingRule, tableMetas, sqlRouteResult, parameters);
        sqlRewriteBuilder.addSQLTokenGenerators(new ShardingTokenGenerateBuilder(shardingRule, sqlRouteResult).getSQLTokenGenerators());
    }
}
