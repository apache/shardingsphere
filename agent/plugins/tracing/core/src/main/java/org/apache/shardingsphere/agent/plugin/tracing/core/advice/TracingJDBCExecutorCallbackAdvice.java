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

package org.apache.shardingsphere.agent.plugin.tracing.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;

/**
 * Tracing JDBC executor callback advice executor.
 * 
 * @param <T> type of root span
 */
public abstract class TracingJDBCExecutorCallbackAdvice<T> extends AbstractInstanceMethodAdvice {
    
    protected static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    public final void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
        JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
        ResourceMetaData resourceMetaData = AgentReflectionUtils.getFieldValue(target, "resourceMetaData");
        ConnectionProperties connectionProps = resourceMetaData.getStorageUnits().get(executionUnit.getExecutionUnit().getDataSourceName()).getConnectionProperties();
        DatabaseType storageType = resourceMetaData.getStorageUnits().get(executionUnit.getExecutionUnit().getDataSourceName()).getStorageType();
        recordExecuteInfo(RootSpanContext.get(), target, executionUnit, (boolean) args[1], connectionProps, storageType);
    }
    
    protected abstract void recordExecuteInfo(T parentSpan, TargetAdviceObject target, JDBCExecutionUnit executionUnit,
                                              boolean isTrunkThread, ConnectionProperties connectionProps, DatabaseType databaseType);
}
