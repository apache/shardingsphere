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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.exception.connection.CursorNameNotFoundException;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.CursorToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;

import java.util.Optional;

/**
 * Sharding cursor token generator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class ShardingCursorTokenGenerator implements OptionalSQLTokenGenerator<SQLStatementContext> {
    
    private final ShardingRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<CursorSQLStatementAttribute> cursorSQLStatementAttribute = sqlStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class);
        return cursorSQLStatementAttribute.isPresent() && cursorSQLStatementAttribute.get().getCursorName().isPresent();
    }
    
    @Override
    public SQLToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        CursorSQLStatementAttribute cursorSQLStatementAttribute = sqlStatementContext.getSqlStatement().getAttributes().getAttribute(CursorSQLStatementAttribute.class);
        CursorNameSegment cursorNameSegment = cursorSQLStatementAttribute.getCursorName().orElseThrow(CursorNameNotFoundException::new);
        return new CursorToken(cursorNameSegment.getStartIndex(), cursorNameSegment.getStopIndex(), cursorNameSegment.getIdentifier(), sqlStatementContext, rule);
    }
}
