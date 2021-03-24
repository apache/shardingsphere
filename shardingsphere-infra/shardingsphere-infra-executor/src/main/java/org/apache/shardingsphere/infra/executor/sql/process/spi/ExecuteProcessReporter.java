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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;

/**
 * Execute process report.
 */
public interface ExecuteProcessReporter {
    
    /**
     * Report the summary of this task.
     * @param context context
     * @param executionGroupContext execution group context
     * @param constants constants
     */
    void report(SQLStatementContext<?> context, ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, ExecuteProcessConstants constants);
    
    /**
     * Report a unit of this task.
     * @param executionID execution ID
     * @param executionUnit execution unit
     * @param constants constants
     */
    void report(String executionID, SQLExecutionUnit executionUnit, ExecuteProcessConstants constants);
}
