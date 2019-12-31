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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.merge.dal.DALEncryptMergeEngine;
import org.apache.shardingsphere.encrypt.merge.dql.DQLEncryptMergeEngine;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sharding.merge.MergeEngineFactory;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergedResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Merge entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class MergeEntry {
    
    private final DatabaseType databaseType;
    
    private final RelationMetas relationMetas;
    
    private final Collection<BaseRule> rules;
    
    @Getter
    private final SQLRouteResult routeResult;
    
    private final boolean queryWithCipherColumn;
    
    /**
     * Get merged result.
     * 
     * @param queryResults query results
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult getMergedResult(final List<QueryResult> queryResults) throws SQLException {
        // TODO process sharding + encrypt for desc table
        Optional<ShardingRule> shardingRule = findShardingRule();
        Optional<EncryptRule> encryptRule = findEncryptRule();
        Preconditions.checkState(shardingRule.isPresent() || encryptRule.isPresent());
        if (encryptRule.isPresent() && routeResult.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            return new DALEncryptMergeEngine(encryptRule.get(), queryResults, routeResult.getSqlStatementContext()).merge();
        }
        MergedResult mergedResult;
        if (shardingRule.isPresent()) {
            mergedResult = MergeEngineFactory.newInstance(databaseType, shardingRule.get(), routeResult, relationMetas, queryResults).merge();
        } else {
            mergedResult = new IteratorStreamMergedResult(queryResults);
        }
        return encryptRule.isPresent() ? new DQLEncryptMergeEngine(createEncryptorMetaData(encryptRule.get()), mergedResult, queryWithCipherColumn).merge() : mergedResult;
    }
    
    private Optional<ShardingRule> findShardingRule() {
        for (BaseRule each : rules) {
            if (each instanceof ShardingRule) {
                return Optional.of((ShardingRule) each);
            }
        }
        return Optional.absent();
    }
    
    private Optional<EncryptRule> findEncryptRule() {
        for (BaseRule each : rules) {
            if (each instanceof EncryptRule && !((EncryptRule) each).getEncryptTableNames().isEmpty()) {
                return Optional.of((EncryptRule) each);
            }
        }
        return Optional.absent();
    }
    
    protected abstract EncryptorMetaData createEncryptorMetaData(EncryptRule encryptRule);
}
