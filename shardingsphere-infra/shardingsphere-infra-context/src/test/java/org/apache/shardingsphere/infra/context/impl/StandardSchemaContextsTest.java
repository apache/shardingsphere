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

package org.apache.shardingsphere.infra.context.impl;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StandardSchemaContextsTest {
    
    @Test
    public void assertGetDefaultSchemaContext() {
        StandardSchemaContexts standardSchemaContexts = new StandardSchemaContexts();
        SchemaContext expected = mock(SchemaContext.class);
        standardSchemaContexts.getSchemaContextMap().put("logic_db", expected);
        assertThat(standardSchemaContexts.getDefaultSchemaContext(), is(expected));
    }
    
    @Test
    public void assertClose() {
        RuntimeContext runtimeContext = mock(RuntimeContext.class, RETURNS_DEEP_STUBS);
        ExecutorKernel executorKernel = mock(ExecutorKernel.class);
        when(runtimeContext.getExecutorKernel()).thenReturn(executorKernel);
        SchemaContext schemaContext = new SchemaContext("logic_db", mock(ShardingSphereSchema.class), runtimeContext);
        StandardSchemaContexts standardSchemaContexts = new StandardSchemaContexts(
                Collections.singletonMap("logic_db", schemaContext), new Authentication(), new ConfigurationProperties(new Properties()), mock(DatabaseType.class));
        standardSchemaContexts.close();
        verify(executorKernel).close();
    }
}
