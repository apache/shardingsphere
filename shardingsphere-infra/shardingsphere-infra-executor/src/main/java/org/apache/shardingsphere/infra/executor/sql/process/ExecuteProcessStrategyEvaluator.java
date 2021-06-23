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

package org.apache.shardingsphere.infra.executor.sql.process;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;

/**
 * Process strategy evaluator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecuteProcessStrategyEvaluator {
    
    /**
     * Evaluate.
     *
     * @param context context
     * @param executionGroupContext execution group context
     * @param props configuration properties
     * @return submit or not
     */
    public static boolean evaluate(final SQLStatementContext<?> context, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ConfigurationProperties props) {
        // TODO : Add more conditions to evaluate whether to submit this process task or not
        boolean showProcessListEnabled = props.getValue(ConfigurationPropertyKey.SHOW_PROCESS_LIST_ENABLED);
        return showProcessListEnabled && context.getSqlStatement() instanceof DDLStatement;
    }
}
