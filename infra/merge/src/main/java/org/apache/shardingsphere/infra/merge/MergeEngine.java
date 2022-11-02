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

package org.apache.shardingsphere.infra.merge;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngineFactory;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMergerEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Merge engine.
 */
public final class MergeEngine {
    
    private final ShardingSphereDatabase database;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, ResultProcessEngine> engines;
    
    private final ConnectionContext connectionContext;
    
    public MergeEngine(final ShardingSphereDatabase database, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        this.database = database;
        this.props = props;
        engines = ResultProcessEngineFactory.getInstances(database.getRuleMetaData().getRules());
        this.connectionContext = connectionContext;
    }
    
    /**
     * Merge.
     *
     * @param queryResults query results
     * @param sqlStatementContext SQL statement context
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        Optional<MergedResult> mergedResult = executeMerge(queryResults, sqlStatementContext);
        Optional<MergedResult> result = mergedResult.isPresent() ? Optional.of(decorate(mergedResult.get(), sqlStatementContext)) : decorate(queryResults.get(0), sqlStatementContext);
        return result.orElseGet(() -> new TransparentMergedResult(queryResults.get(0)));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Optional<MergedResult> executeMerge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        for (Entry<ShardingSphereRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultMergerEngine) {
                ResultMerger resultMerger = ((ResultMergerEngine) entry.getValue()).newInstance(database.getName(), database.getProtocolType(), entry.getKey(), props, sqlStatementContext);
                return Optional.of(resultMerger.merge(queryResults, sqlStatementContext, database, connectionContext));
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private MergedResult decorate(final MergedResult mergedResult, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        MergedResult result = null;
        for (Entry<ShardingSphereRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultDecoratorEngine) {
                ResultDecorator resultDecorator = ((ResultDecoratorEngine) entry.getValue()).newInstance(database, entry.getKey(), props, sqlStatementContext);
                result = null == result ? resultDecorator.decorate(mergedResult, sqlStatementContext, entry.getKey()) : resultDecorator.decorate(result, sqlStatementContext, entry.getKey());
            }
        }
        return null == result ? mergedResult : result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Optional<MergedResult> decorate(final QueryResult queryResult, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        MergedResult result = null;
        for (Entry<ShardingSphereRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultDecoratorEngine) {
                ResultDecorator resultDecorator = ((ResultDecoratorEngine) entry.getValue()).newInstance(database, entry.getKey(), props, sqlStatementContext);
                result = null == result ? resultDecorator.decorate(queryResult, sqlStatementContext, entry.getKey()) : resultDecorator.decorate(result, sqlStatementContext, entry.getKey());
            }
        }
        return Optional.ofNullable(result);
    }
}
