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
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collection;
import java.util.Collections;
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
     * @param sqlStatement SQL statement
     * @param rule rule
     * @return SQL tokens
     */
    @SuppressWarnings("unchecked")
    public final List<SQLToken> generateSQLTokens(final SQLStatement sqlStatement, final T rule) {
        List<SQLToken> result = new LinkedList<>(sqlStatement.getSQLTokens());
        for (SQLTokenGenerator each : getSQLTokenGenerators()) {
            Optional<? extends SQLToken> sqlToken = each.generateSQLToken(sqlStatement, rule);
            if (sqlToken.isPresent()) {
                result.add(sqlToken.get());
            }
        }
        Collections.sort(result);
        return result;
    }
    
    protected abstract Collection<SQLTokenGenerator> getSQLTokenGenerators();
}
