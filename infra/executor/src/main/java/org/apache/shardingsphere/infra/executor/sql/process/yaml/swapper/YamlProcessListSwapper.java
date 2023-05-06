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
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessList;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * YAML process list swapper.
 */
public final class YamlProcessListSwapper implements YamlConfigurationSwapper<YamlProcessList, Collection<Process>> {
    
    private final YamlProcessSwapper yamlProcessSwapper = new YamlProcessSwapper();
    
    @Override
    public YamlProcessList swapToYamlConfiguration(final Collection<Process> data) {
        YamlProcessList result = new YamlProcessList();
        result.setProcesses(data.stream().map(yamlProcessSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public Collection<Process> swapToObject(final YamlProcessList yamlConfig) {
        return yamlConfig.getProcesses().stream().map(yamlProcessSwapper::swapToObject).collect(Collectors.toList());
    }
}
