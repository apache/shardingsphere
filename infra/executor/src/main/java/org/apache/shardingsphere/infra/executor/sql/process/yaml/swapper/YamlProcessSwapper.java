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

package org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper;

import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcess;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * YAML process swapper.
 */
public final class YamlProcessSwapper implements YamlConfigurationSwapper<YamlProcess, Process> {
    
    @Override
    public YamlProcess swapToYamlConfiguration(final Process data) {
        YamlProcess result = new YamlProcess();
        result.setId(data.getId());
        result.setStartMillis(data.getStartMillis());
        result.setSql(data.getSql());
        result.setDatabaseName(data.getDatabaseName());
        result.setUsername(data.getUsername());
        result.setHostname(data.getHostname());
        result.setTotalUnitCount(data.getTotalUnitCount());
        result.setCompletedUnitCount(data.getCompletedUnitCount());
        result.setIdle(data.isIdle());
        result.setInterrupted(data.isInterrupted());
        return result;
    }
    
    @Override
    public Process swapToObject(final YamlProcess yamlConfig) {
        return new Process(yamlConfig.getId(), yamlConfig.getStartMillis(), yamlConfig.getSql(), yamlConfig.getDatabaseName(), yamlConfig.getUsername(), yamlConfig.getHostname(),
                yamlConfig.getTotalUnitCount(), new AtomicInteger(yamlConfig.getCompletedUnitCount()), yamlConfig.isIdle(), new AtomicBoolean(yamlConfig.isInterrupted()));
    }
}
