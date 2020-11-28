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

package org.apache.shardingsphere.scaling.core.spi;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ScalingWorkerLoaderTest {
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertWithoutDistributedScalingService() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        assertFalse(ScalingWorkerLoader.initScalingWorker().isPresent());
    }
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertScalingWorkerAvailable() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", mockServerConfiguration());
        assertTrue(ScalingWorkerLoader.initScalingWorker().isPresent());
    }
    
    private ServerConfiguration mockServerConfiguration() {
        ServerConfiguration result = new ServerConfiguration();
        YamlGovernanceConfiguration distributedScalingService = new YamlGovernanceConfiguration();
        YamlGovernanceCenterConfiguration registryCenter = new YamlGovernanceCenterConfiguration();
        registryCenter.setType("Zookeeper");
        distributedScalingService.setRegistryCenter(registryCenter);
        result.setDistributedScalingService(distributedScalingService);
        return result;
    }
}
