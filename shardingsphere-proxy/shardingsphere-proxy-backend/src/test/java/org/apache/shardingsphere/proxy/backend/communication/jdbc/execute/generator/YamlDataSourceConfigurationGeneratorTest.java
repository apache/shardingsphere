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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.generator;

import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext.DataSourceContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class YamlDataSourceConfigurationGeneratorTest {
    
    private CreateDataSourcesStatementContext sqlStatement;
    
    @Before
    public void setUp() {
        sqlStatement = mock(CreateDataSourcesStatementContext.class);
        when(sqlStatement.getDataSourceContexts()).thenReturn(createDataSourceContexts());
    }
    
    private Collection<DataSourceContext> createDataSourceContexts() {
        Collection<DataSourceContext> result = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            DataSourceContext context =
                    new DataSourceContext("ds" + i, "jdbc:mysql://127.0.0.1:3306/demo_ds_" + i + "?serverTimezone=UTC&useSSL=false", "root" + i, "root" + i);
            result.add(context);
        }
        return result;
    }
    
    @Test
    public void assertGenerate() {
        Map<String, YamlDataSourceParameter> result = new YamlDataSourceConfigurationGenerator().generate(sqlStatement);
        assertThat(result.size(), is(2));
        assertTrue(result.keySet().containsAll(Arrays.asList("ds0", "ds1")));
        assertThat(result.values().iterator().next().getUsername(), is("root0"));
    }
}
