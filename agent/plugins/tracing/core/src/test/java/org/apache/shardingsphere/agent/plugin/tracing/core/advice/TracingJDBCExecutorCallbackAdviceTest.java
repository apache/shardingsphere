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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingJDBCExecutorCallbackAdviceTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @BeforeEach
    void setUp() {
        RootSpanContext.set("root-span");
    }
    
    @AfterEach
    void reset() {
        RootSpanContext.set(null);
    }
    
    @Test
    void assertBeforeMethod() {
        JDBCExecutionUnit expectedJdbcExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit("select 1", Collections.emptyList())), ConnectionMode.MEMORY_STRICTLY, mock());
        ConnectionProperties expectedConnectionProps = new ConnectionProperties("127.0.0.1", 3306, "catalog", "schema", new Properties());
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(storageUnit.getConnectionProperties()).thenReturn(expectedConnectionProps);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        RecordingTracingJDBCExecutorCallbackAdvice advice = new RecordingTracingJDBCExecutorCallbackAdvice();
        TargetAdviceObject expectedTarget = new ResourceTargetAdviceObject(resourceMetaData);
        advice.beforeMethod(expectedTarget, new TargetAdviceMethod("execute"), new Object[]{expectedJdbcExecutionUnit, false}, "FIXTURE");
        assertThat(advice.getRecordedParentSpan(), is("root-span"));
        assertThat(advice.getRecordedTarget(), is(expectedTarget));
        assertThat(advice.getRecordedExecutionUnit(), is(expectedJdbcExecutionUnit));
        assertFalse(advice.getRecordedIsTrunkThread());
        assertThat(advice.getRecordedConnectionProps(), is(expectedConnectionProps));
        assertThat(advice.getRecordedDatabaseType(), is(databaseType));
    }
    
    private static final class RecordingTracingJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<String> {
        
        private String recordedParentSpan;
        
        private TargetAdviceObject recordedTarget;
        
        private JDBCExecutionUnit recordedExecutionUnit;
        
        private boolean recordedIsTrunkThread;
        
        private ConnectionProperties recordedConnectionProps;
        
        private DatabaseType recordedDatabaseType;
        
        @Override
        protected void recordExecuteInfo(final String parentSpan, final TargetAdviceObject target, final JDBCExecutionUnit executionUnit,
                                         final boolean isTrunkThread, final ConnectionProperties connectionProps, final DatabaseType databaseType) {
            recordedParentSpan = parentSpan;
            recordedTarget = target;
            recordedExecutionUnit = executionUnit;
            recordedIsTrunkThread = isTrunkThread;
            recordedConnectionProps = connectionProps;
            recordedDatabaseType = databaseType;
        }
        
        String getRecordedParentSpan() {
            return recordedParentSpan;
        }
        
        TargetAdviceObject getRecordedTarget() {
            return recordedTarget;
        }
        
        JDBCExecutionUnit getRecordedExecutionUnit() {
            return recordedExecutionUnit;
        }
        
        boolean getRecordedIsTrunkThread() {
            return recordedIsTrunkThread;
        }
        
        ConnectionProperties getRecordedConnectionProps() {
            return recordedConnectionProps;
        }
        
        DatabaseType getRecordedDatabaseType() {
            return recordedDatabaseType;
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    @Setter
    private static final class ResourceTargetAdviceObject implements TargetAdviceObject {
        
        private final ResourceMetaData resourceMetaData;
        
        private Object attachment;
    }
}
