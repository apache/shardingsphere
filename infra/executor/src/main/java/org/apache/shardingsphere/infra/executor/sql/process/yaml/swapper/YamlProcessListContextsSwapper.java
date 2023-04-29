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

import org.apache.shardingsphere.infra.executor.sql.process.model.ProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessListContexts;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * YAML process list contexts swapper.
 */
public final class YamlProcessListContextsSwapper implements YamlConfigurationSwapper<YamlProcessListContexts, Collection<ProcessContext>> {
    
    private final YamlProcessContextSwapper yamlProcessContextSwapper = new YamlProcessContextSwapper();
    
    @Override
    public YamlProcessListContexts swapToYamlConfiguration(final Collection<ProcessContext> data) {
        YamlProcessListContexts result = new YamlProcessListContexts();
        result.setContexts(data.stream().map(yamlProcessContextSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public Collection<ProcessContext> swapToObject(final YamlProcessListContexts yamlConfig) {
        throw new UnsupportedOperationException("YamlProcessListContextsSwapper.swapToObject");
    }
}
