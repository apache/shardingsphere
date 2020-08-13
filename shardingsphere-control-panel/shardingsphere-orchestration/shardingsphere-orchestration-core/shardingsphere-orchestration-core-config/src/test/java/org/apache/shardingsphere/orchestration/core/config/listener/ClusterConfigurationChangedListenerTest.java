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

package org.apache.shardingsphere.orchestration.core.config.listener;

import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.core.common.event.ClusterConfigurationChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ClusterConfigurationChangedListenerTest {
    
    private static final String DATA_CLUSTER_YAML = "yaml/configCenter/data-cluster.yaml";
    
    private ClusterConfigurationChangedListener clusterConfigurationChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        clusterConfigurationChangedListener = new ClusterConfigurationChangedListener("test", configurationRepository);
    }
    
    @Test
    public void assertCreateOrchestrationEvent() {
        ClusterConfigurationChangedEvent event = clusterConfigurationChangedListener
                .createOrchestrationEvent(new DataChangedEvent("test", readYAML(DATA_CLUSTER_YAML), ChangedType.UPDATED));
        assertNotNull(event);
        assertNotNull(event.getClusterConfiguration());
        assertNotNull(event.getClusterConfiguration().getHeartbeat());
        assertThat(event.getClusterConfiguration().getHeartbeat().getSql(), is("select 1"));
        assertThat(event.getClusterConfiguration().getHeartbeat().getThreadCount(), is(1));
        assertThat(event.getClusterConfiguration().getHeartbeat().getInterval(), is(60));
        assertFalse(event.getClusterConfiguration().getHeartbeat().isRetryEnable());
        assertThat(event.getClusterConfiguration().getHeartbeat().getRetryMaximum(), is(3));
        assertThat(event.getClusterConfiguration().getHeartbeat().getRetryInterval(), is(3));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
