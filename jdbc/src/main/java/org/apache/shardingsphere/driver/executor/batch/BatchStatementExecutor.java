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

package org.apache.shardingsphere.driver.executor.batch;

import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * Batch executor for {@link Statement}.
 */
@RequiredArgsConstructor
public final class BatchStatementExecutor {
    
    private final Statement statement;
    
    private final List<String> batchedSQLs = new LinkedList<>();
    
    /**
     * Add SQL for batched execution.
     *
     * @param sql SQL 
     */
    public void addBatch(final String sql) {
        batchedSQLs.add(sql);
    }
    
    /**
     * Execute batched SQLs.
     *
     * @return execute results
     * @throws SQLException SQL exception
     */
    public int[] executeBatch() throws SQLException {
        int[] result = new int[batchedSQLs.size()];
        int index = 0;
        for (String each : batchedSQLs) {
            result[index++] = statement.executeUpdate(each);
        }
        return result;
    }
    
    /**
     * Clear batched SQLs.
     */
    public void clear() {
        batchedSQLs.clear();
    }
}
