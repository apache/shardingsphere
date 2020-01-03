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

package org.apache.shardingsphere.underlying.merge;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.underlying.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.underlying.merge.engine.merger.ResultMergerEngine;
import org.apache.shardingsphere.underlying.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.underlying.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;
import org.apache.shardingsphere.underlying.merge.result.impl.transparent.TransparentMergedResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Merge entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MergeEntry {
    
    private final DatabaseType databaseType;
    
    private final RelationMetas relationMetas;
    
    private final ShardingSphereProperties properties;
    
    private final Map<BaseRule, ResultProcessEngine> engines;
    
    /**
     * Process query results.
     * 
     * @param queryResults query results
     * @param sqlStatementContext SQL statement context
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult process(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        Optional<MergedResult> mergedResult = merge(queryResults, sqlStatementContext);
        Optional<MergedResult> result = mergedResult.isPresent() ? Optional.of(decorate(mergedResult.get(), sqlStatementContext)) : decorate(queryResults.get(0), sqlStatementContext);
        return result.isPresent() ? result.get() : new TransparentMergedResult(queryResults.get(0));
    }
    
    @SuppressWarnings("unchecked")
    private Optional<MergedResult> merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        for (Entry<BaseRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultMergerEngine) {
                ResultMerger resultMerger = ((ResultMergerEngine) entry.getValue()).newInstance(databaseType, entry.getKey(), properties, sqlStatementContext);
                return Optional.of(resultMerger.merge(queryResults, sqlStatementContext, relationMetas));
            }
        }
        return Optional.absent();
    }
    
    @SuppressWarnings("unchecked")
    private MergedResult decorate(final MergedResult mergedResult, final SQLStatementContext sqlStatementContext) throws SQLException {
        MergedResult result = null;
        for (Entry<BaseRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultDecoratorEngine) {
                ResultDecorator resultDecorator = ((ResultDecoratorEngine) entry.getValue()).newInstance(databaseType, entry.getKey(), properties, sqlStatementContext);
                result = null == result ? resultDecorator.decorate(mergedResult, sqlStatementContext, relationMetas) : resultDecorator.decorate(result, sqlStatementContext, relationMetas);
            }
        }
        return null == result ? mergedResult : result;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<MergedResult> decorate(final QueryResult queryResult, final SQLStatementContext sqlStatementContext) throws SQLException {
        MergedResult result = null;
        for (Entry<BaseRule, ResultProcessEngine> entry : engines.entrySet()) {
            if (entry.getValue() instanceof ResultDecoratorEngine) {
                ResultDecorator resultDecorator = ((ResultDecoratorEngine) entry.getValue()).newInstance(databaseType, entry.getKey(), properties, sqlStatementContext);
                result = null == result ? resultDecorator.decorate(queryResult, sqlStatementContext, relationMetas) : resultDecorator.decorate(result, sqlStatementContext, relationMetas);
            }
        }
        return Optional.fromNullable(result);
    }
}
