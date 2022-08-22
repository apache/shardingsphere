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

package org.apache.shardingsphere.infra.executor.sql.process.spi;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.optional.OptionalSPI;

/**
 * Execute process report.
 */
@SingletonSPI
public interface ExecuteProcessReporter extends OptionalSPI {
    
    /**
     * Report the summary of this task.
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @param constants constants
     * @param eventBusContext event bus context                 
     */
    void report(QueryContext queryContext, ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, ExecuteProcessConstants constants, EventBusContext eventBusContext);
    
    /**
     * Report a unit of this task.
     * @param executionID execution ID
     * @param executionUnit execution unit
     * @param constants constants
     * @param eventBusContext event bus context                    
     */
    void report(String executionID, SQLExecutionUnit executionUnit, ExecuteProcessConstants constants, EventBusContext eventBusContext);
    
    /**
     * Report this task on completion.
     * @param executionID execution ID
     * @param constants constants
     * @param eventBusContext event bus context                  
     */
    void report(String executionID, ExecuteProcessConstants constants, EventBusContext eventBusContext);
    
    /**
     * Report clean the task.
     * 
     * @param executionID execution ID
     */
    void reportClean(String executionID);
}
