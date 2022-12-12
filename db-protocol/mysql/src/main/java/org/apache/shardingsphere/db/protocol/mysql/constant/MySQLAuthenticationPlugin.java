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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * MySQL client/server protocol authentication plugins.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/authentication-plugins.html">Authentication Plugins</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLAuthenticationPlugin {
    
    DEFAULT(null),
    
    NATIVE_PASSWORD_AUTHENTICATION("mysql_native_password"),
    
    SHA2_AUTHENTICATION("caching_sha2_password"),
    
    SHA256_AUTHENTICATION("sha256_password");
    
    private static final Map<String, MySQLAuthenticationPlugin> VALUE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1);
    
    static {
        for (MySQLAuthenticationPlugin each : values()) {
            VALUE_AND_COLUMN_TYPE_MAP.put(each.getPluginName(), each);
        }
    }
    
    private final String pluginName;
    
    /**
     * Get plugin by name.
     *
     * @param pluginName plugin name
     * @return mysql authentication plugin
     */
    public static MySQLAuthenticationPlugin getPluginByName(final String pluginName) {
        if (VALUE_AND_COLUMN_TYPE_MAP.containsKey(pluginName)) {
            return VALUE_AND_COLUMN_TYPE_MAP.get(pluginName);
        }
        return DEFAULT;
    }
}
