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

package org.apache.shardingsphere.core.rewrite.token.generator.collection.impl;

import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.IndexToken;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Index token generator.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class IndexTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    public Collection<IndexToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<IndexToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatementContext.getSqlStatement().getAllSQLSegments()) {
            if (each instanceof IndexSegment) {
                result.add(createIndexToken((IndexSegment) each));
            }
        }
        return result;
    }
    
    private IndexToken createIndexToken(final IndexSegment segment) {
        return new IndexToken(segment.getStartIndex(), segment.getStopIndex(), segment.getName(), segment.getQuoteCharacter());
    }
}
