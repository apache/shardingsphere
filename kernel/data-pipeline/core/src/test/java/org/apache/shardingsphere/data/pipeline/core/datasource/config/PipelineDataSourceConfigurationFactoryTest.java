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

package org.apache.shardingsphere.data.pipeline.core.datasource.config;

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PipelineDataSourceConfigurationFactoryTest {
    
    @Test
    void assertNewInstanceForStandardPipelineDataSourceConfiguration() {
        assertThat(PipelineDataSourceConfigurationFactory.newInstance(StandardPipelineDataSourceConfiguration.TYPE, "url: jdbc:mock://127.0.0.1/foo_db"),
                isA(StandardPipelineDataSourceConfiguration.class));
    }
    
    @Test
    void assertNewInstanceForShardingSpherePipelineDataSourceConfiguration() {
        assertThat(PipelineDataSourceConfigurationFactory.newInstance(ShardingSpherePipelineDataSourceConfiguration.TYPE, "dataSources:\n" + "  foo_ds:\n" + "    url: jdbc:mock://127.0.0.1/foo_db"),
                isA(ShardingSpherePipelineDataSourceConfiguration.class));
    }
    
    @Test
    void assertNewInstanceForUnsupportedType() {
        assertThrows(UnsupportedSQLOperationException.class, () -> PipelineDataSourceConfigurationFactory.newInstance("Invalid", ""));
    }
}
