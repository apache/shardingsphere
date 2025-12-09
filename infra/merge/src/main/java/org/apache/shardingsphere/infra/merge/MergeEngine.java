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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.impl.TransparentResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMergerEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Merge engine.
 */
@HighFrequencyInvocation
public final class MergeEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereDatabase database;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, ResultProcessEngine> engines;
    
    private final ConnectionContext connectionContext;
    
    public MergeEngine(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        this.metaData = metaData;
        this.database = database;
        this.props = props;
        engines = OrderedSPILoader.getServices(ResultProcessEngine.class, database.getRuleMetaData().getRules());
        this.connectionContext = connectionContext;
    }
    
    /**
     * Merge.
     *
     * @param queryResults query results
     * @param queryContext SQL statement context
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult merge(final List<QueryResult> queryResults, final QueryContext queryContext) throws SQLException {
        MergedResult mergedResult = executeMerge(queryResults, queryContext).orElseGet(() -> new TransparentMergedResult(queryResults.get(0)));
        return decorate(mergedResult, queryContext);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Optional<MergedResult> executeMerge(final List<QueryResult> queryResults, final QueryContext queryContext) throws SQLException {
        for (Entry<ShardingSphereRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultMergerEngine) {
                ResultMerger resultMerger =
                        ((ResultMergerEngine) entry.getValue()).newInstance(database.getName(), database.getProtocolType(), entry.getKey(), props, queryContext.getSqlStatementContext());
                return Optional.of(resultMerger.merge(queryResults, queryContext.getSqlStatementContext(), database, connectionContext));
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private MergedResult decorate(final MergedResult mergedResult, final QueryContext queryContext) throws SQLException {
        MergedResult result = null;
        for (Entry<ShardingSphereRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultDecoratorEngine) {
                ResultDecorator resultDecorator = getResultDecorator(queryContext.getSqlStatementContext(), entry.getValue());
                result = null == result ? resultDecorator.decorate(mergedResult, queryContext, entry.getKey())
                        : resultDecorator.decorate(result, queryContext, entry.getKey());
            }
        }
        return null == result ? mergedResult : result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResultDecorator getResultDecorator(final SQLStatementContext sqlStatementContext, final ResultProcessEngine resultProcessEngine) {
        return (ResultDecorator) ((ResultDecoratorEngine) resultProcessEngine).newInstance(metaData, database, props, sqlStatementContext).orElseGet(TransparentResultDecorator::new);
    }
}
