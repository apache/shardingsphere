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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLContainerConfigurationFactory {
    
    /**
     * Create new instance of mysql container configuration.
     * 
     * @return created instance
     */
    public static MySQLContainerConfiguration newInstance() {
        return new MySQLContainerConfiguration(getCommands(), getContainerEnvs(), getResourceMappings());
    }
    
    private static String[] getCommands() {
        // TODO need auto set server-id by generator, now always set server-id to 1
        String[] commands = new String[1];
        commands[0] = "--server-id=1";
        return commands;
    }
    
    private static Map<String, String> getContainerEnvs() {
        Map<String, String> result = new HashMap<>();
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        return result;
    }
    
    private static Map<String, String> getResourceMappings() {
        return Collections.singletonMap("/env/mysql/my.cnf", "/etc/mysql/my.cnf");
    }
}
