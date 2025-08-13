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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OffsetToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;

/**
 * Sharding offset token generator.
 */
@HighFrequencyInvocation
public final class ShardingOffsetTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext
                && ((SelectStatementContext) sqlStatementContext).getPaginationContext().getOffsetSegment().isPresent()
                && ((SelectStatementContext) sqlStatementContext).getPaginationContext().getOffsetSegment().get() instanceof NumberLiteralPaginationValueSegment;
    }
    
    @Override
    public OffsetToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        PaginationContext pagination = selectStatementContext.getPaginationContext();
        Preconditions.checkState(pagination.getOffsetSegment().isPresent());
        return new OffsetToken(pagination.getOffsetSegment().get().getStartIndex(), pagination.getOffsetSegment().get().getStopIndex(), pagination.getRevisedOffset());
    }
}
