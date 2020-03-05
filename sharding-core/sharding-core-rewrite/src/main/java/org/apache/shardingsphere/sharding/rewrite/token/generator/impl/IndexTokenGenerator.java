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

import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.IndexToken;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.IndexSegmentAvailable;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.IndexSegmentsAvailable;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Index token generator.
 */
public final class IndexTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getSqlStatement() instanceof IndexSegmentAvailable && null != ((IndexSegmentAvailable) sqlStatementContext.getSqlStatement()).getIndex()) {
            return true;
        }
        if (sqlStatementContext.getSqlStatement() instanceof IndexSegmentsAvailable && !((IndexSegmentsAvailable) sqlStatementContext.getSqlStatement()).getIndexes().isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public Collection<IndexToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<IndexToken> result = new LinkedList<>();
        if (sqlStatementContext.getSqlStatement() instanceof IndexSegmentAvailable) {
            IndexSegment indexSegment = ((IndexSegmentAvailable) sqlStatementContext.getSqlStatement()).getIndex();
            result.add(new IndexToken(indexSegment.getStartIndex(), indexSegment.getStopIndex(), indexSegment.getIdentifier()));
        }
        if (sqlStatementContext.getSqlStatement() instanceof IndexSegmentsAvailable) {
            for (SQLSegment each : ((IndexSegmentsAvailable) sqlStatementContext.getSqlStatement()).getIndexes()) {
                result.add(new IndexToken(each.getStartIndex(), each.getStopIndex(), ((IndexSegment) each).getIdentifier()));
            }
        }
        
        return result;
    }
}
