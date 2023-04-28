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

package org.apache.shardingsphere.infra.executor.sql.process.model.yaml.swapper;

import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.stream.Collectors;

/**
 * YAML execute process context swapper.
 */
public final class YamlExecuteProcessContextSwapper implements YamlConfigurationSwapper<YamlExecuteProcessContext, ExecuteProcessContext> {
    
    private final YamlExecuteProcessUnitSwapper yamlExecuteProcessUnitSwapper = new YamlExecuteProcessUnitSwapper();
    
    @Override
    public YamlExecuteProcessContext swapToYamlConfiguration(final ExecuteProcessContext data) {
        YamlExecuteProcessContext result = new YamlExecuteProcessContext();
        result.setExecutionID(data.getExecutionID());
        result.setDatabaseName(data.getDatabaseName());
        result.setUsername(data.getUsername());
        result.setHostname(data.getHostname());
        result.setSql(data.getSql());
        result.setUnitStatuses(data.getProcessUnits().values().stream().map(yamlExecuteProcessUnitSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        result.setStartTimeMillis(data.getStartMillis());
        result.setProcessStatus(data.getStatus());
        return result;
    }
    
    @Override
    public ExecuteProcessContext swapToObject(final YamlExecuteProcessContext yamlConfig) {
        throw new UnsupportedOperationException("YamlExecuteProcessContextSwapper.swapToObject");
    }
}
