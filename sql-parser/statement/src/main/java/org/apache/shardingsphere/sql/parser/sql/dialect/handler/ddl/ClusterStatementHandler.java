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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLClusterStatement;

import java.util.Optional;

/**
 * Cluster statement handler for different dialect SQLStatements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterStatementHandler implements SQLStatementHandler {
    
    /**
     * Get simple table segment.
     *
     * @param clusterStatement cluster statement
     * @return simple table segment
     */
    public static Optional<SimpleTableSegment> getSimpleTableSegment(final ClusterStatement clusterStatement) {
        if (clusterStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLClusterStatement) clusterStatement).getTable();
        }
        return Optional.empty();
    }
    
    /**
     * Get index segment.
     *
     * @param clusterStatement cluster statement
     * @return index segment
     */
    public static Optional<IndexSegment> getIndexSegment(final ClusterStatement clusterStatement) {
        if (clusterStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLClusterStatement) clusterStatement).getIndex();
        }
        return Optional.empty();
    }
}
