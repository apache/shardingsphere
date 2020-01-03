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

package org.apache.shardingsphere.underlying.merge.engine.merger;

import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Query results merger.
 *
 * @author zhangliang
 */
public interface ResultMerger {
    
    /**
     * Merge query results.
     * 
     * @param queryResults query results
     * @param sqlStatementContext SQL statement context
     * @param relationMetas relation metas
     * @return merged result
     * @throws SQLException SQL exception
     */
    MergedResult merge(List<QueryResult> queryResults, SQLStatementContext sqlStatementContext, RelationMetas relationMetas) throws SQLException;
}
