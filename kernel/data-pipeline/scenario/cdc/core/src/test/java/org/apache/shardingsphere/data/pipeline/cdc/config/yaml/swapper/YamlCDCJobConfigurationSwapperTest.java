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

package org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration.SinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlCDCJobConfigurationSwapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertSwapToObject() {
        YamlCDCJobConfiguration yamlJobConfig = createYamlCDCJobConfiguration();
        CDCJobConfiguration actual = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        assertThat(actual.getJobId(), is("j0302p00007a8bf46da145dc155ba25c710b550220"));
        assertThat(actual.getDatabaseName(), is("test_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("test.t_order", "t_order_item")));
        assertTrue(actual.isFull());
    }
    
    private YamlCDCJobConfiguration createYamlCDCJobConfiguration() {
        YamlCDCJobConfiguration result = new YamlCDCJobConfiguration();
        result.setJobId("j0302p00007a8bf46da145dc155ba25c710b550220");
        result.setDatabaseName("test_db");
        result.setSchemaTableNames(Arrays.asList("test.t_order", "t_order_item"));
        result.setFull(true);
        result.setSourceDatabaseType("MySQL");
        YamlSinkConfiguration sinkConfig = new YamlSinkConfiguration();
        sinkConfig.setSinkType(CDCSinkType.SOCKET.name());
        result.setSinkConfig(sinkConfig);
        return result;
    }
    
    @Test
    void assertSwapToYamlConfig() {
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("j0302p00007a8bf46da145dc155ba25c710b550220", "test_db", Arrays.asList("t_order", "t_order_item"), true, databaseType,
                null, null, null, true, new SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 1);
        YamlCDCJobConfiguration actual = new YamlCDCJobConfigurationSwapper().swapToYamlConfiguration(jobConfig);
        assertThat(actual.getJobId(), is("j0302p00007a8bf46da145dc155ba25c710b550220"));
        assertThat(actual.getDatabaseName(), is("test_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("t_order", "t_order_item")));
        assertTrue(actual.isFull());
    }
}
