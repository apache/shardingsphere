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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertCipherNameToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert logic column names token generator.
 *
 * @author panjuan
 */
public final class InsertCipherNameTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<InsertCipherNameToken> generateSQLTokens(final OptimizedStatement optimizedStatement,
                                                               final ParameterBuilder parameterBuilder, final EncryptRule rule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Collections.emptyList();
        }
        return createInsertColumnTokens((InsertOptimizedStatement) optimizedStatement, rule);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    private Collection<InsertCipherNameToken> createInsertColumnTokens(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!insertColumnsSegment.isPresent()) {
            return Collections.emptyList();
        }
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(optimizedStatement.getTables().getSingleTableName());
        Collection<InsertCipherNameToken> result = new LinkedList<>();
        for (ColumnSegment each : insertColumnsSegment.get().getColumns()) {
            if (logicAndCipherColumns.keySet().contains(each.getName())) {
                result.add(new InsertCipherNameToken(each.getStartIndex(), each.getStopIndex(), logicAndCipherColumns.get(each.getName())));
            }
        }
        return result;
    }
}
