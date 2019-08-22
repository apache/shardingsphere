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
import org.apache.shardingsphere.core.optimize.api.segment.OptimizedInsertValue;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertValuesToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Insert values token generator.
 *
 * @author panjuan
 */
public final class InsertValuesTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Optional<InsertValuesToken> generateSQLToken(final OptimizedStatement optimizedStatement, 
                                                        final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        Collection<InsertValuesSegment> insertValuesSegments = optimizedStatement.getSQLStatement().findSQLSegments(InsertValuesSegment.class);
        return isNeedToGenerateSQLToken(optimizedStatement, insertValuesSegments)
                ? Optional.of(createInsertValuesToken((InsertOptimizedStatement) optimizedStatement, insertValuesSegments)) : Optional.<InsertValuesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement, final Collection<InsertValuesSegment> insertValuesSegments) {
        return optimizedStatement.getSQLStatement() instanceof InsertStatement && !insertValuesSegments.isEmpty();
    }
    
    private InsertValuesToken createInsertValuesToken(final InsertOptimizedStatement optimizedStatement, final Collection<InsertValuesSegment> insertValuesSegments) {
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        for (OptimizedInsertValue each : optimizedStatement.getOptimizedInsertValues()) {
            result.addInsertValueToken(Arrays.asList(each.getValueExpressions()), each.getDataNodes());
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> insertValuesSegments) {
        int result = insertValuesSegments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : insertValuesSegments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> insertValuesSegments) {
        int result = insertValuesSegments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : insertValuesSegments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
}
