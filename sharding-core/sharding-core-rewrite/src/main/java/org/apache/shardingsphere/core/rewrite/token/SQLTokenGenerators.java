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
import org.apache.shardingsphere.core.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL token generators.
 * 
 * @author zhangliang
 */
public final class SQLTokenGenerators {
    
    private final Collection<SQLTokenGenerator> sqlTokenGenerators = new LinkedList<>();
    
    /**
     * Add all SQL token generators.
     * 
     * @param sqlTokenGenerators SQL token generators
     */
    public void addAll(final Collection<SQLTokenGenerator> sqlTokenGenerators) {
        for (SQLTokenGenerator each : sqlTokenGenerators) {
            if (!containsClass(each)) {
                this.sqlTokenGenerators.add(each);
            }
        }
    }
    
    private boolean containsClass(final SQLTokenGenerator sqlTokenGenerator) {
        for (SQLTokenGenerator each : sqlTokenGenerators) {
            if (each.getClass() == sqlTokenGenerator.getClass()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param rewriteStatement rewrite statement
     * @param parameterBuilder SQL parameter builder
     * @param tableMetas table metas
     * @param isSingleRoute is single route
     * @return SQL tokens
     */
    @SuppressWarnings("unchecked")
    public List<SQLToken> generateSQLTokens(final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder, final TableMetas tableMetas, final boolean isSingleRoute) {
        List<SQLToken> result = new LinkedList<>();
        for (SQLTokenGenerator each : sqlTokenGenerators) {
            if (isSingleRoute && each instanceof IgnoreForSingleRoute) {
                continue;
            }
            if (each instanceof TableMetasAware) {
                ((TableMetasAware) each).setTableMetas(tableMetas);
            }
            if (each instanceof OptionalSQLTokenGenerator) {
                Optional<? extends SQLToken> sqlToken = ((OptionalSQLTokenGenerator) each).generateSQLToken(rewriteStatement, parameterBuilder);
                if (sqlToken.isPresent()) {
                    result.add(sqlToken.get());
                }
            } else {
                result.addAll(((CollectionSQLTokenGenerator) each).generateSQLTokens(rewriteStatement, parameterBuilder));
            }
        }
        Collections.sort(result);
        return result;
    }
}
