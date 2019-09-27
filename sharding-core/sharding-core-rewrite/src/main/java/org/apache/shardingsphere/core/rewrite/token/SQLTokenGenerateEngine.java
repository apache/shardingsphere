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

package org.apache.shardingsphere.core.rewrite.token;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL token generator.
 *
 * @author zhangliang
 * 
 * @param <T> type of rule 
 */
public abstract class SQLTokenGenerateEngine<T extends BaseRule> {
    
    /**
     * Generate SQL tokens.
     *
     * @param rewriteStatement rewrite statement
     * @param parameterBuilder SQL parameter builder
     * @param rule rule
     * @param tableMetas table metas
     * @param isSingleRoute is single route
     * @return SQL tokens
     */
    @SuppressWarnings("unchecked")
    public final List<SQLToken> generateSQLTokens(final RewriteStatement rewriteStatement, 
                                                  final ParameterBuilder parameterBuilder, final T rule, final TableMetas tableMetas, final boolean isSingleRoute) {
        List<SQLToken> result = new LinkedList<>();
        for (SQLTokenGenerator each : getSQLTokenGenerators()) {
            if (isSingleRoute && each instanceof IgnoreForSingleRoute) {
                continue;
            }
            if (each instanceof TableMetasAware) {
                ((TableMetasAware) each).setTableMetas(tableMetas);
            }
            setAutowaredProperties(each);
            if (each instanceof OptionalSQLTokenGenerator) {
                Optional<? extends SQLToken> sqlToken = ((OptionalSQLTokenGenerator) each).generateSQLToken(rewriteStatement, parameterBuilder, rule);
                if (sqlToken.isPresent()) {
                    result.add(sqlToken.get());
                }
            } else {
                result.addAll(((CollectionSQLTokenGenerator) each).generateSQLTokens(rewriteStatement, parameterBuilder, rule));
            }
        }
        return result;
    }
    
    protected void setAutowaredProperties(final SQLTokenGenerator sqlTokenGenerator) {
    }
    
    protected abstract Collection<SQLTokenGenerator> getSQLTokenGenerators();
}
