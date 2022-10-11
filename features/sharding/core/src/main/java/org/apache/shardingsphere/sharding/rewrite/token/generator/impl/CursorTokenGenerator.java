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
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.CursorToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;

/**
 * Cursor token generator.
 */
@Setter
public final class CursorTokenGenerator implements OptionalSQLTokenGenerator<SQLStatementContext<?>>, ShardingRuleAware {
    
    private ShardingRule shardingRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof CursorAvailable && ((CursorAvailable) sqlStatementContext).getCursorName().isPresent();
    }
    
    @Override
    public SQLToken generateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        Preconditions.checkArgument(sqlStatementContext instanceof CursorAvailable, "SQLStatementContext must implementation CursorAvailable interface.");
        Preconditions.checkArgument(((CursorAvailable) sqlStatementContext).getCursorName().isPresent(), "Can not get cursor name from SQLStatementContext.");
        CursorNameSegment cursorName = ((CursorAvailable) sqlStatementContext).getCursorName().get();
        return new CursorToken(cursorName.getStartIndex(), cursorName.getStopIndex(), cursorName.getIdentifier(), sqlStatementContext, shardingRule);
    }
}
