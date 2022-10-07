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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineDataSourceFactoryTest {
    
    @Test
    public void assertNewInstance() {
        Map<String, Object> yamlDataSourceConfig = new HashMap<>(3, 1);
        yamlDataSourceConfig.put("url", "jdbc:mysql://localhost:3306/database");
        yamlDataSourceConfig.put("username", "username");
        yamlDataSourceConfig.put("password", "password");
        PipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
        assertThat(PipelineDataSourceFactory.newInstance(pipelineDataSourceConfig).getDatabaseType(), is(pipelineDataSourceConfig.getDatabaseType()));
    }
}
