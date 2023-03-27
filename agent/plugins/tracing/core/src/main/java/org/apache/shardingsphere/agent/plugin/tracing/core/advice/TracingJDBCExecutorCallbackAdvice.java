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

import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Tracing JDBC executor callback advice executor.
 * 
 * @param <T> type of root span
 */
public abstract class TracingJDBCExecutorCallbackAdvice<T> implements InstanceMethodAdvice {
    
    protected static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    public final void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
        Map<String, DatabaseType> storageTypes = AgentReflectionUtils.getFieldValue(target, "storageTypes");
        DatabaseType storageType = storageTypes.get(executionUnit.getExecutionUnit().getDataSourceName());
        DataSourceMetaData metaData = AgentReflectionUtils.invokeMethod(
                JDBCExecutorCallback.class.getDeclaredMethod("getDataSourceMetaData", DatabaseMetaData.class, DatabaseType.class),
                target, executionUnit.getStorageResource().getConnection().getMetaData(), storageType);
        recordExecuteInfo(RootSpanContext.get(), target, executionUnit, (boolean) args[1], metaData, storageType.getType());
    }
    
    protected abstract void recordExecuteInfo(T parentSpan, TargetAdviceObject target, JDBCExecutionUnit executionUnit, boolean isTrunkThread, DataSourceMetaData metaData, String databaseType);
}
