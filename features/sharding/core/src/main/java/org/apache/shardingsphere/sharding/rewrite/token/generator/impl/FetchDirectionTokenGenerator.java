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

import lombok.Setter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.ConnectionContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.FetchDirectionToken;
import org.apache.shardingsphere.sql.parser.sql.common.enums.DirectionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;

/**
 * Fetch direction token generator.
 */
@Setter
public final class FetchDirectionTokenGenerator implements OptionalSQLTokenGenerator<SQLStatementContext>, ConnectionContextAware {
    
    private ConnectionContext connectionContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof FetchStatementContext;
    }
    
    @Override
    public SQLToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        FetchStatement fetchStatement = ((FetchStatementContext) sqlStatementContext).getSqlStatement();
        CursorNameSegment cursorName = fetchStatement.getCursorName();
        int startIndex = fetchStatement.getDirection().map(DirectionSegment::getStartIndex).orElseGet("FETCH"::length);
        int stopIndex = fetchStatement.getDirection().map(DirectionSegment::getStopIndex).orElseGet("FETCH"::length);
        DirectionType directionType = fetchStatement.getDirection().flatMap(DirectionSegment::getDirectionType).orElse(DirectionType.NEXT);
        long fetchCount = fetchStatement.getDirection().flatMap(DirectionSegment::getCount).orElse(1L);
        return new FetchDirectionToken(startIndex, stopIndex, directionType, fetchCount, cursorName.getIdentifier().getValue().toLowerCase(), connectionContext);
    }
    
    @Override
    public void setConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }
}
