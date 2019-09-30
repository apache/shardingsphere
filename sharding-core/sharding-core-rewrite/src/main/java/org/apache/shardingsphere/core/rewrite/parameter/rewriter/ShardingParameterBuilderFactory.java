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

package org.apache.shardingsphere.core.rewrite.parameter.rewriter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Parameter builder factory for sharding.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingParameterBuilderFactory {
    
    /**
     * Build parameter builder.
     *
     * @param encryptRule encrypt rule
     * @param tableMetas table metas
     * @param sqlRouteResult SQL route result
     * @param parameters SQL parameters
     * @param queryWithCipherColumn query with cipher column
     * @return parameter builder
     */
    public static ParameterBuilder build(final EncryptRule encryptRule, final TableMetas tableMetas, 
                                         final SQLRouteResult sqlRouteResult, final List<Object> parameters, final boolean queryWithCipherColumn) {
        ParameterBuilder result = ParameterBuilderFactory.newInstance(sqlRouteResult.getSqlStatementContext(), parameters, sqlRouteResult);
        for (ParameterRewriter each : getParameterRewriters()) {
            if (each instanceof EncryptRuleAware) {
                ((EncryptRuleAware) each).setEncryptRule(encryptRule);
            }
            if (each instanceof TableMetasAware) {
                ((TableMetasAware) each).setTableMetas(tableMetas);
            }
            if (each instanceof QueryWithCipherColumnAware) {
                ((QueryWithCipherColumnAware) each).setQueryWithCipherColumn(queryWithCipherColumn);
            }
            each.rewrite(sqlRouteResult.getSqlStatementContext(), result);
        }
        return result;
    }
    
    private static Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        result.add(new EncryptWhereParameterRewriter());
        return result;
    }
}
