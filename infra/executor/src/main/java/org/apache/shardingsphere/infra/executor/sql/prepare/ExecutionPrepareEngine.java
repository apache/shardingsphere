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

package org.apache.shardingsphere.infra.executor.sql.prepare;

import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Execution prepare engine.
 * 
 * @param <T> type of input value
 */
public interface ExecutionPrepareEngine<T> {
    
    /**
     * Prepare to execute.
     *
     * @param databaseName database name
     * @param executionContext execution context
     * @param executionUnits execution units
     * @param reportContext report context
     * @return execution group context
     * @throws SQLException SQL exception
     */
    ExecutionGroupContext<T> prepare(String databaseName, ExecutionContext executionContext, Collection<ExecutionUnit> executionUnits, ExecutionGroupReportContext reportContext) throws SQLException;
    
    /**
     * Prepare to execute.
     *
     * @param databaseName database name
     * @param executionContext execution context
     * @param connectionOffsets execution offsets
     * @param executionUnits execution units
     * @param reportContext report context
     * @return execution group context
     * @throws SQLException SQL exception
     */
    ExecutionGroupContext<T> prepare(String databaseName, ExecutionContext executionContext, Map<String, Integer> connectionOffsets, Collection<ExecutionUnit> executionUnits,
                                     ExecutionGroupReportContext reportContext) throws SQLException;
}
