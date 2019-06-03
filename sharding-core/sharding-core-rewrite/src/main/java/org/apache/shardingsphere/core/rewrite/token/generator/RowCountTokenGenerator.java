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
import org.apache.shardingsphere.core.parse.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.sql.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.RowCountToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Row count token generator.
 *
 * @author panjuan
 */
public final class RowCountTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<RowCountToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.absent();
        }
        if (isExistedOfNumberRowCount(sqlStatement)) {
            LimitValueSegment rowCount = sqlStatement.findSQLSegment(LimitSegment.class).get().getRowCount().get();
            return Optional.of(new RowCountToken(rowCount.getStartIndex(), rowCount.getStopIndex(), ((NumberLiteralLimitValueSegment) rowCount).getValue()));
        }
        if (isExistedOfLimitRowNum((SelectStatement) sqlStatement)) {
            LimitValue rowCount = ((SelectStatement) sqlStatement).getLimit().getRowCount();
            return Optional.of(new RowCountToken(rowCount.getLimitValueSegment().getStartIndex(), rowCount.getLimitValueSegment().getStopIndex(), rowCount.getValue()));
        }
        return Optional.absent();
    }
    
    private boolean isExistedOfLimitRowNum(final SelectStatement sqlStatement) {
        Limit limit = sqlStatement.getLimit();
        return null != limit && null != limit.getRowCount() && -1 != limit.getRowCount().getValue();
    }
    
    private boolean isExistedOfNumberRowCount(final SQLStatement sqlStatement) {
        Optional<LimitSegment> limitSegment = sqlStatement.findSQLSegment(LimitSegment.class);
        return limitSegment.isPresent() && limitSegment.get().getRowCount().isPresent() && limitSegment.get().getRowCount().get() instanceof NumberLiteralLimitValueSegment;
    }
}
