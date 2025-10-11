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

package org.apache.shardingsphere.test.e2e.env.container.util.spi;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * SQL batch execution strategy.
 *
 * <p>Allow different drivers to decide whether to use addBatch/executeBatch or fallback to execute one by one.</p>
 */
public interface SQLBatchExecutionStrategy extends TypedSPI {
    
    /**
     * Whether this strategy supports the given JDBC driver name.
     *
     * @param jdbcDriverName driver name from connection metadata
     * @return true if supported
     */
    boolean supports(String jdbcDriverName);
    
    /**
     * Execute SQLs using the strategy.
     *
     * @param connection connection
     * @param statement statement
     * @param sqls SQL collection
     * @throws SQLException SQL exception
     */
    void execute(Connection connection, Statement statement, Collection<String> sqls) throws SQLException;
}
