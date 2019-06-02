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
import org.apache.shardingsphere.core.rewrite.token.pojo.OffsetToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Row count token generator.
 *
 * @author panjuan
 */
public final class RowCountTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<OffsetToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.absent();
        }
        if (isExistedOfNumberOffset(sqlStatement)) {
            LimitValueSegment offset = sqlStatement.findSQLSegment(LimitSegment.class).get().getOffset().get();
            return Optional.of(new OffsetToken(offset.getStartIndex(), offset.getStopIndex(), ((NumberLiteralLimitValueSegment) offset).getValue()));
        }
        if (isExistedOfLimitRowNum((SelectStatement) sqlStatement)) {
            LimitValue offset = ((SelectStatement) sqlStatement).getLimit().getOffset();
            return Optional.of(new OffsetToken(offset.getSqlSegment().getStartIndex(), offset.getSqlSegment().getStopIndex(), offset.getValue()));
        }
        return Optional.absent();
    }
    
    private boolean isExistedOfLimitRowNum(final SelectStatement sqlStatement) {
        Limit limit = sqlStatement.getLimit();
        return null != limit && null != limit.getOffset() && -1 != limit.getOffset().getValue();
    }
    
    private boolean isExistedOfNumberOffset(final SQLStatement sqlStatement) {
        Optional<LimitSegment> limitSegment = sqlStatement.findSQLSegment(LimitSegment.class);
        return limitSegment.isPresent() && limitSegment.get().getOffset().isPresent() && limitSegment.get().getOffset().get() instanceof NumberLiteralLimitValueSegment;
    }
}
