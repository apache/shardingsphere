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

package org.apache.shardingsphere.infra.executor.sql.process.model.yaml;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Batch execute process context for YAML.
 */
@NoArgsConstructor
@Getter
@Setter
public final class BatchYamlExecuteProcessContext {
    
    private Collection<YamlExecuteProcessContext> contexts;
    
    public BatchYamlExecuteProcessContext(final Collection<ExecuteProcessContext> processContexts) {
        this.contexts = getYamlProcessContexts(processContexts);
    }
    
    private Collection<YamlExecuteProcessContext> getYamlProcessContexts(final Collection<ExecuteProcessContext> processContexts) {
        Collection<YamlExecuteProcessContext> result = new LinkedList<>();
        for (ExecuteProcessContext each : processContexts) {
            result.add(new YamlExecuteProcessContext(each));
        }
        return result;
    }
}
