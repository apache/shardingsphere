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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.junit.Test;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

public final class LogicSchemaTest {

    @Test
    public void assertDataSourcesReturned() throws NoSuchFieldException, IllegalAccessException {
        LogicSchema absSchema = Mockito.mock(LogicSchema.class, Mockito.CALLS_REAL_METHODS);

        JDBCBackendDataSource jdbcBackendDataSource = Mockito.mock(JDBCBackendDataSource.class);

        Field backendDataSourceField = LogicSchema.class.getDeclaredField("backendDataSource");
        backendDataSourceField.setAccessible(true);
        backendDataSourceField.set(absSchema, jdbcBackendDataSource);


        Map<String, YamlDataSourceParameter> expectedMap = Maps.newHashMap();
        when(jdbcBackendDataSource.getDataSourceParameters()).thenReturn(expectedMap);

        Map<String, YamlDataSourceParameter> dataSourceParameterMap = absSchema.getDataSources();


        assertNotNull(dataSourceParameterMap);
        assertThat(dataSourceParameterMap, is(expectedMap));
    }
}

