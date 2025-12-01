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

package org.apache.shardingsphere.sharding.merge.dal;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Sharding dialect DAL result merger.
 */
@SingletonSPI
public interface DialectShardingDALResultMerger extends DatabaseTypedSPI {
    
    /**
     * Merge DAL statement result set.
     *
     * @param databaseName database name
     * @param rule sharding rule
     * @param sqlStatementContext SQL statement context
     * @param schema schema
     * @param queryResults query results
     * @return merged result
     * @throws SQLException SQL exception
     */
    Optional<MergedResult> merge(String databaseName, ShardingRule rule, SQLStatementContext sqlStatementContext, ShardingSphereSchema schema, List<QueryResult> queryResults) throws SQLException;
}
