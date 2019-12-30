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

package org.apache.shardingsphere.core.merge;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.merge.dal.DALEncryptMergeEngine;
import org.apache.shardingsphere.encrypt.merge.dql.DQLEncryptMergeEngine;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sharding.merge.MergeEngineFactory;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergedResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Merge entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MergeEntry {
    
    private final DatabaseType databaseType;
    
    private final RelationMetas relationMetas;
    
    private final ShardingRule shardingRule;
    
    private final EncryptRule encryptRule;
    
    private final SQLRouteResult routeResult;
    
    private final boolean queryWithCipherColumn;
    
    /**
     * Get merged result.
     * 
     * @param queryResults query results
     * @param encryptorMetaData encryptor meta data
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult getMergedResult(final List<QueryResult> queryResults, final EncryptorMetaData encryptorMetaData) throws SQLException {
        // TODO process sharding + encrypt for desc table
        if (!encryptRule.getEncryptTableNames().isEmpty() && routeResult.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            return new DALEncryptMergeEngine(encryptRule, queryResults, routeResult.getSqlStatementContext()).merge();
        }
        MergedResult mergedResult = MergeEngineFactory.newInstance(databaseType, shardingRule, routeResult, relationMetas, queryResults).merge();
        return encryptRule.getEncryptTableNames().isEmpty() ? mergedResult : new DQLEncryptMergeEngine(encryptorMetaData, mergedResult, queryWithCipherColumn).merge();
    }
}
