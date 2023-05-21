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

package org.apache.shardingsphere.test.e2e.transaction.framework.container.config.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLContainerConfigurationFactory {
    
    /**
     * Create new instance of MySQL container configuration.
     *
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance() {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources());
    }
    
    private static String getCommand() {
        return "--sql_mode= --default-authentication-plugin=mysql_native_password";
    }
    
    private static Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        result.put("MYSQL_ROOT_HOST", "%");
        return result;
    }
    
    private static Map<String, String> getMountedResources() {
        return Collections.singletonMap("/env/mysql/my.cnf", MySQLContainer.MYSQL_CONF_IN_CONTAINER);
    }
}
