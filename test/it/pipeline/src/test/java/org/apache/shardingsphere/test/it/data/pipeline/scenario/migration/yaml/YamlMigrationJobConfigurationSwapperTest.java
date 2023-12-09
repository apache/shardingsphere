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

package org.apache.shardingsphere.test.it.data.pipeline.scenario.migration.yaml;

import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.config.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlMigrationJobConfigurationSwapperTest {
    
    @Test
    void assertMarsharlUnmarshal() {
        YamlMigrationJobConfiguration yamlJobConfig = JobConfigurationBuilder.createYamlMigrationJobConfiguration();
        YamlMigrationJobConfigurationSwapper swapper = new YamlMigrationJobConfigurationSwapper();
        MigrationJobConfiguration jobConfig = swapper.swapToObject(yamlJobConfig);
        YamlMigrationJobConfiguration actual = swapper.swapToYamlConfiguration(jobConfig);
        assertThat(YamlEngine.marshal(actual), is(YamlEngine.marshal(yamlJobConfig)));
    }
}
