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
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.OffsetToken;
import org.apache.shardingsphere.core.route.pagination.Pagination;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collections;

/**
 * Offset token generator.
 *
 * @author panjuan
 */
public final class OffsetTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule>, IgnoreForSingleRoute {
    
    @Override
    public Optional<OffsetToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.absent();
        }
        Optional<PaginationValueSegment> offsetSegment = getLiteralOffsetSegment((SelectStatement) sqlStatement);
        return offsetSegment.isPresent()
                ? Optional.of(new OffsetToken(offsetSegment.get().getStartIndex(), offsetSegment.get().getStopIndex(), getRevisedOffset((SelectStatement) sqlStatement, offsetSegment.get())))
                : Optional.<OffsetToken>absent();
    }
    
    private Optional<PaginationValueSegment> getLiteralOffsetSegment(final SelectStatement selectStatement) {
        return isLiteralOffset(selectStatement) ? Optional.of(selectStatement.getOffset()) : Optional.<PaginationValueSegment>absent();
    }
    
    private boolean isLiteralOffset(final SelectStatement selectStatement) {
        return null != selectStatement.getOffset() && selectStatement.getOffset() instanceof NumberLiteralPaginationValueSegment;
    }
    
    private int getRevisedOffset(final SelectStatement sqlStatement, final PaginationValueSegment offsetSegment) {
        return new Pagination(offsetSegment, sqlStatement.getRowCount(), Collections.emptyList()).getRevisedOffset();
    }
}
