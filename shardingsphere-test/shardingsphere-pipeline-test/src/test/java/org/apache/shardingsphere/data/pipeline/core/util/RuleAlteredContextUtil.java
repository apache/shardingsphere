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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Properties;

public final class RuleAlteredContextUtil {
    
    /**
     * Raw mock context configuration.
     */
    @SneakyThrows
    public static void rawMockContextConfig() {
        RuleAlteredContext context = RuleAlteredContext.getInstance();
        ReflectionUtil.setFieldValue(context, "onRuleAlteredActionConfig", createOnRuleAlteredActionConfig());
        ReflectionUtil.setFieldValue(context, "modeConfig", createModeConfig());
    }
    
    /**
     * Initialize and mock context configuration.
     */
    @SneakyThrows
    public static void initAndMockContextConfig() {
        RuleAlteredContext context = RuleAlteredContext.getInstance();
        ReflectionUtil.setFieldValue(context, "onRuleAlteredActionConfig", null);
        ReflectionUtil.setFieldValue(context, "modeConfig", null);
        context.init(createOnRuleAlteredActionConfig());
        context.initModeConfig(createModeConfig());
    }
    
    private static OnRuleAlteredActionConfiguration createOnRuleAlteredActionConfig() {
        return new OnRuleAlteredActionConfiguration(1000, 5, new ShardingSphereAlgorithmConfiguration("FIXTURE", new Properties()),
                new ShardingSphereAlgorithmConfiguration("FIXTURE", new Properties()));
    }
    
    private static ModeConfiguration createModeConfig() {
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), new Properties()), true);
    }
}
