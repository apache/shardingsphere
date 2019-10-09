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

package org.apache.shardingsphere.core.rewrite.encrypt.parameter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.encrypt.parameter.impl.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.core.rewrite.encrypt.parameter.impl.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.core.rewrite.encrypt.parameter.impl.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Parameter builder factory for encrypt.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptParameterBuilderFactory {
    
    /**
     * Build parameter builder.
     *
     * @param parameterBuilder parameter builder
     * @param encryptRule encrypt rule
     * @param tableMetas table metas
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param queryWithCipherColumn query with cipher column
     */
    public static void build(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final TableMetas tableMetas,
                                         final SQLStatementContext sqlStatementContext, final List<Object> parameters, final boolean queryWithCipherColumn) {
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
            each.rewrite(parameterBuilder, sqlStatementContext, parameters);
        }
    }
    
    private static Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        result.add(new EncryptAssignmentParameterRewriter());
        result.add(new EncryptPredicateParameterRewriter());
        result.add(new EncryptInsertValueParameterRewriter());
        return result;
    }
}
