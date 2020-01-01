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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.merge.DecoratorEngineFactory;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sharding.merge.MergeEngineFactory;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.engine.DecoratorEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;
import org.apache.shardingsphere.underlying.merge.result.impl.transparent.TransparentMergedResult;

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
    
    private final boolean queryWithCipherColumn;
    
    /**
     * Get merged result.
     * 
     * @param queryResults query results
     * @param sqlStatementContext SQL statementContext
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult getMergedResult(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        Optional<ShardingRule> shardingRule = findShardingRule();
        MergedResult result = null;
        if (shardingRule.isPresent()) {
            result = MergeEngineFactory.newInstance(databaseType, shardingRule.get(), sqlStatementContext).merge(queryResults, sqlStatementContext, relationMetas);
        }
        Optional<EncryptRule> encryptRule = findEncryptRule();
        if (encryptRule.isPresent()) {
            EncryptorMetaData encryptorMetaData = createEncryptorMetaData(encryptRule.get(), sqlStatementContext);
            DecoratorEngine decoratorEngine = DecoratorEngineFactory.newInstance(encryptRule.get(), sqlStatementContext, encryptorMetaData, queryWithCipherColumn);
            if (null == result) {
                result = new TransparentMergedResult(queryResults.get(0));
            }
            result = decoratorEngine.decorate(result, sqlStatementContext, relationMetas);
        }
        Preconditions.checkNotNull(result);
        return result;
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
    
    protected abstract EncryptorMetaData createEncryptorMetaData(EncryptRule encryptRule, SQLStatementContext sqlStatementContext);
}
