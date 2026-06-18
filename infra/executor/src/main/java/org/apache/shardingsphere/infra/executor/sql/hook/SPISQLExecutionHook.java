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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.List;

/**
 * SQL Execution hook for SPI.
 */
public final class SPISQLExecutionHook implements SQLExecutionHook {
    
    private final Collection<SQLExecutionHook> sqlExecutionHooks = ShardingSphereServiceLoader.getServiceInstances(SQLExecutionHook.class);
    
    @Override
    public void start(final String dataSourceName, final String sql, final List<Object> params, final ConnectionProperties connectionProps, final boolean isTrunkThread) {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.start(dataSourceName, sql, params, connectionProps, isTrunkThread);
        }
    }
    
    @Override
    public void finishSuccess() {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.finishSuccess();
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.finishFailure(cause);
        }
    }
}
