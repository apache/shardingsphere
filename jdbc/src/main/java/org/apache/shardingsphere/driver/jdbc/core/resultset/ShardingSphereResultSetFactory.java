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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere result set factory.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class ShardingSphereResultSetFactory {
    
    private final ConnectionContext connectionContext;
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final Collection<Statement> statements;
    
    /**
     * Create new instance of shardingSphere result set.
     * 
     * @param database database
     * @param queryContext query context
     * @param queryResults query results
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @return created instance
     * @throws SQLException SQL exception
     */
    public ShardingSphereResultSet newInstance(final ShardingSphereDatabase database, final QueryContext queryContext, final List<QueryResult> queryResults,
                                               final Statement statement, final Map<String, Integer> columnLabelAndIndexMap) throws SQLException {
        List<ResultSet> resultSets = getResultSets();
        MergedResult mergedResult = new MergeEngine(globalRuleMetaData, database, props, connectionContext).merge(queryResults, queryContext.getSqlStatementContext());
        return new ShardingSphereResultSet(resultSets, mergedResult, statement, queryContext.getSqlStatementContext(),
                null == columnLabelAndIndexMap
                        ? ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(queryContext.getSqlStatementContext(), resultSets.get(0).getMetaData())
                        : columnLabelAndIndexMap);
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private List<ResultSet> getResultSets() throws SQLException {
        List<ResultSet> result = new ArrayList<>(statements.size());
        for (Statement each : statements) {
            if (null != each.getResultSet()) {
                result.add(each.getResultSet());
            }
        }
        return result;
    }
}
