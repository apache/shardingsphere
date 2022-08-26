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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Execute process context for YAML.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class YamlExecuteProcessContext {
    
    private String executionID;
    
    private String databaseName;
    
    private String username;
    
    private String hostname;
    
    private String sql;
    
    private Collection<YamlExecuteProcessUnit> unitStatuses;
    
    private Long startTimeMillis;
    
    public YamlExecuteProcessContext(final ExecuteProcessContext executeProcessContext) {
        executionID = executeProcessContext.getExecutionID();
        databaseName = executeProcessContext.getDatabaseName();
        username = executeProcessContext.getUsername();
        hostname = executeProcessContext.getHostname();
        sql = executeProcessContext.getSql();
        unitStatuses = new ArrayList<>(executeProcessContext.getProcessUnits().size());
        for (ExecuteProcessUnit each : executeProcessContext.getProcessUnits().values()) {
            unitStatuses.add(new YamlExecuteProcessUnit(each));
        }
        startTimeMillis = executeProcessContext.getStartTimeMillis();
    }
}
