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

package org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultMySQLContainerConfiguration implements StorageContainerConfiguration {
    
    private final String scenario;
    
    @Override
    public String[] getCommands() {
        // TODO need auto set server-id by generator, now always set server-id to 1
        String[] commands = new String[1];
        commands[0] = "--server-id=1";
        return commands;
    }
    
    @Override
    public Map<String, String> getEnvs() {
        return ImmutableMap.<String, String>builder().put("LANG", "C.UTF-8").put("MYSQL_RANDOM_ROOT_PASSWORD", "yes").build();
    }
    
    @Override
    public Map<String, String> getResourceMappings() {
        return ImmutableMap.<String, String>builder().put("/env/mysql/my.cnf", "/etc/mysql/my.cnf").build();
    }
}
