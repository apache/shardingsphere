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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.config.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

final class YamlConsistencyCheckJobConfigurationSwapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final YamlConsistencyCheckJobConfigurationSwapper swapper = new YamlConsistencyCheckJobConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        Properties algorithmProps = PropertiesBuilder.build(new Property("chunk-size", "1000"));
        YamlConsistencyCheckJobConfiguration actual = swapper.swapToYamlConfiguration(new ConsistencyCheckJobConfiguration("foo_job", "parent_job", "FIXTURE", algorithmProps, databaseType));
        assertThat(actual.getJobId(), is("foo_job"));
        assertThat(actual.getParentJobId(), is("parent_job"));
        assertThat(actual.getAlgorithmTypeName(), is("FIXTURE"));
        assertThat(actual.getAlgorithmProps(), is(algorithmProps));
        assertThat(actual.getSourceDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithoutSourceDatabaseType() {
        YamlConsistencyCheckJobConfiguration actual = swapper.swapToYamlConfiguration(new ConsistencyCheckJobConfiguration("foo_job", "parent_job", "FIXTURE", new Properties(), null));
        assertThat(actual.getJobId(), is("foo_job"));
        assertThat(actual.getParentJobId(), is("parent_job"));
        assertThat(actual.getAlgorithmTypeName(), is("FIXTURE"));
        assertThat(actual.getAlgorithmProps(), is(new Properties()));
        assertNull(actual.getSourceDatabaseType());
    }
    
    @Test
    void assertSwapToObjectFromYamlConfiguration() {
        YamlConsistencyCheckJobConfiguration yamlConfig = new YamlConsistencyCheckJobConfiguration();
        yamlConfig.setJobId("foo_job");
        yamlConfig.setParentJobId("parent_job");
        yamlConfig.setAlgorithmTypeName("FIXTURE");
        yamlConfig.setAlgorithmProps(new Properties());
        ConsistencyCheckJobConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getJobId(), is("foo_job"));
        assertThat(actual.getParentJobId(), is("parent_job"));
        assertThat(actual.getAlgorithmTypeName(), is("FIXTURE"));
        assertThat(actual.getSourceDatabaseType(), is(nullValue()));
    }
    
    @Test
    void assertSwapToObjectFromJobParam() {
        String jobParam = "jobId: foo_job\n"
                + "parentJobId: parent_job\n"
                + "algorithmTypeName: FIXTURE\n"
                + "algorithmProps:\n"
                + "  chunk-size: 64\n"
                + "sourceDatabaseType: H2\n";
        ConsistencyCheckJobConfiguration actual = swapper.swapToObject(jobParam);
        assertThat(actual.getJobId(), is("foo_job"));
        assertThat(actual.getParentJobId(), is("parent_job"));
        assertThat(actual.getAlgorithmTypeName(), is("FIXTURE"));
        assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
    }
    
    @Test
    void assertSwapToObjectFromJobParamWithNullJobParam() {
        assertNull(swapper.swapToObject((String) null));
    }
}
