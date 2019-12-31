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

package org.apache.shardingsphere.sharding.merge;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergeEngine;
import org.apache.shardingsphere.underlying.merge.MergedResult;

import java.util.List;

/**
 * Transparent result set merge engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TransparentMergeEngine implements MergeEngine {
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext, final RelationMetas relationMetas) {
        return new IteratorStreamMergedResult(queryResults);
    }
}
