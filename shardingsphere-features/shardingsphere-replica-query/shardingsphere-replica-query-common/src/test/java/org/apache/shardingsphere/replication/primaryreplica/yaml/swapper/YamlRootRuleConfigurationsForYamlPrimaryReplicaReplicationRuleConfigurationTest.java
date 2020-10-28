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

package org.apache.shardingsphere.replication.primaryreplica.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlPrimaryReplicaReplicationRuleConfiguration;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class YamlRootRuleConfigurationsForYamlPrimaryReplicaReplicationRuleConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/replica-query-rule.yaml");
        assertNotNull(url);
        YamlRootRuleConfigurations rootRuleConfigs = YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigs.getRules().size(), is(1));
        assertPrimaryReplicaReplicationRule((YamlPrimaryReplicaReplicationRuleConfiguration) rootRuleConfigs.getRules().iterator().next());
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/replica-query-rule.yaml");
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append(System.lineSeparator());
            }
        }
        YamlRootRuleConfigurations rootRuleConfigs = YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigs.getRules().size(), is(1));
        assertPrimaryReplicaReplicationRule((YamlPrimaryReplicaReplicationRuleConfiguration) rootRuleConfigs.getRules().iterator().next());
    }
    
    private void assertPrimaryReplicaReplicationRule(final YamlPrimaryReplicaReplicationRuleConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(2));
        assertPrimaryReplicaReplicationRuleForDs0(actual);
        assertPrimaryReplicaReplicationRuleForDs1(actual);
    }
    
    private void assertPrimaryReplicaReplicationRuleForDs0(final YamlPrimaryReplicaReplicationRuleConfiguration actual) {
        assertThat(actual.getDataSources().get("ds_0").getPrimaryDataSourceName(), is("primary_ds_0"));
        assertThat(actual.getDataSources().get("ds_0").getReplicaDataSourceNames(), is(Arrays.asList("primary_ds_0_replica_0", "primary_ds_0_replica_1")));
        assertThat(actual.getDataSources().get("ds_0").getLoadBalancerName(), is("roundRobin"));
    }
    
    private void assertPrimaryReplicaReplicationRuleForDs1(final YamlPrimaryReplicaReplicationRuleConfiguration actual) {
        assertThat(actual.getDataSources().get("ds_1").getPrimaryDataSourceName(), is("primary_ds_1"));
        assertThat(actual.getDataSources().get("ds_1").getReplicaDataSourceNames(), is(Arrays.asList("primary_ds_1_replica_0", "primary_ds_1_replica_1")));
        assertThat(actual.getDataSources().get("ds_1").getLoadBalancerName(), is("random"));
    }
}
