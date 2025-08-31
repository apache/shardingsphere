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

package org.apache.shardingsphere.infra.executor.sql.hook;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;

import java.util.List;

/**
 * SQL Execution hook.
 */
public interface SQLExecutionHook extends ShardingSphereSPI {
    
    /**
     * Handle when SQL execution started.
     *
     * @param dataSourceName data source name
     * @param sql SQL
     * @param params SQL parameters
     * @param connectionProps connection properties
     * @param isTrunkThread is execution in trunk thread
     */
    void start(String dataSourceName, String sql, List<Object> params, ConnectionProperties connectionProps, boolean isTrunkThread);
    
    /**
     * Handle when SQL execution finished success.
     */
    void finishSuccess();
    
    /**
     * Handle when SQL execution finished failure.
     *
     * @param cause failure cause
     */
    void finishFailure(Exception cause);
}
