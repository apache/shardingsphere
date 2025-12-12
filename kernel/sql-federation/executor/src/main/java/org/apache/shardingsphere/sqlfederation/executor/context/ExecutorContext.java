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

package org.apache.shardingsphere.sqlfederation.executor.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Executor context.
 */
@RequiredArgsConstructor
@Getter
public final class ExecutorContext {
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    private final Map<String, Integer> connectionOffsets = new LinkedHashMap<>();
    
    private final Collection<ExecutionUnit> previewExecutionUnits = new LinkedList<>();
    
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> queryCallback;
    
    private final ShardingSphereStatistics statistics;
    
    private final String currentDatabaseName;
    
    private final String currentSchemaName;
    
    private final boolean preview;
    
    private final String processId;
}
