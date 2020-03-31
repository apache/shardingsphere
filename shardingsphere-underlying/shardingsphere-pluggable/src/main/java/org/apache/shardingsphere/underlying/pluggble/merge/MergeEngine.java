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

package org.apache.shardingsphere.underlying.pluggble.merge;

import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergeEntry;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Merge engine.
 */
public final class MergeEngine {
    
    private final Collection<BaseRule> rules;
    
    private final MergeEntry merger;
    
    public MergeEngine(final Collection<BaseRule> rules, final ConfigurationProperties properties, final DatabaseType databaseType, final SchemaMetaData metaData) {
        this.rules = rules;
        merger = new MergeEntry(databaseType, metaData, properties);
    }
    
    /**
     * Merge.
     *
     * @param queryResults query results
     * @param sqlStatementContext SQL statement context
     * @return merged result
     * @throws SQLException SQL exception
     */
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        registerMergeDecorator();
        return merger.process(queryResults, sqlStatementContext);
    }
    
    private void registerMergeDecorator() {
        for (ResultProcessEngine each : OrderedSPIRegistry.getRegisteredServices(ResultProcessEngine.class)) {
            Class<?> ruleClass = (Class<?>) each.getType();
            // FIXME rule.getClass().getSuperclass() == ruleClass for orchestration, should decouple extend between orchestration rule and sharding rule
            rules.stream().filter(rule -> rule.getClass() == ruleClass || rule.getClass().getSuperclass() == ruleClass).collect(Collectors.toList())
                    .forEach(rule -> merger.registerProcessEngine(rule, each));
        }
    }
}
