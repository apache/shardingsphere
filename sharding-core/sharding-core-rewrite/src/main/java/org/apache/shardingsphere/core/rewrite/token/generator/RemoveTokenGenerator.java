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

import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.RemoveAvailable;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.RemoveToken;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Remove token generator.
 *
 * @author zhangliang
 */
public final class RemoveTokenGenerator implements CollectionSQLTokenGenerator<BaseRule> {
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final BaseRule baseRule, final boolean isQueryWithCipherColumn) {
        Collection<RemoveToken> result = new LinkedList<>();
        for (SQLSegment each : optimizedStatement.getSQLStatement().getAllSQLSegments()) {
            if (each instanceof RemoveAvailable) {
                result.add(new RemoveToken(each.getStartIndex(), each.getStopIndex()));
            }
        }
        return result;
    }
}
