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

import org.apache.shardingsphere.infra.executor.sql.process.ProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessContext;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML process context swapper.
 */
public final class YamlProcessContextSwapper implements YamlConfigurationSwapper<YamlProcessContext, ProcessContext> {
    
    @Override
    public YamlProcessContext swapToYamlConfiguration(final ProcessContext data) {
        YamlProcessContext result = new YamlProcessContext();
        result.setExecutionID(data.getProcessID());
        result.setDatabaseName(data.getDatabaseName());
        result.setUsername(data.getUsername());
        result.setHostname(data.getHostname());
        result.setSql(data.getSql());
        result.setTotalUnitCount(data.getTotalUnitCount());
        result.setCompletedUnitCount(data.getCompletedUnitCount());
        result.setStartTimeMillis(data.getStartMillis());
        result.setExecuting(data.isExecuting());
        return result;
    }
    
    @Override
    public ProcessContext swapToObject(final YamlProcessContext yamlConfig) {
        throw new UnsupportedOperationException("YamlProcessContextSwapper.swapToObject");
    }
}
