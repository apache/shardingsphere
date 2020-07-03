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

package org.apache.shardingsphere.sharding.route.time;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class PropertiesUtils {
    
    private static final File FILE = new File(PropertiesUtils.class.getResource("/").getPath() + "time-service.properties");
    
    /**
     * Create a time-service.properties file.
     *
     * @param driverClassName driver class name
     * @param sql SQL
     */
    public static void createProperties(final String driverClassName, final String sql) {
        try {
            Properties props = new Properties();
            props.put("dataSourceType", "com.zaxxer.hikari.HikariDataSource");
            props.put("jdbcUrl", "jdbc:test");
            props.put("username", "root");
            props.put("password", "root");
            props.put("driverClassName", driverClassName);
            if (null != sql) {
                props.put("sql", sql);
            }
            FileOutputStream stream = new FileOutputStream(FILE);
            props.store(stream, null);
            stream.close();
        } catch (IOException ignore) {
        }
    }
    
    /**
     * Delete time-service.properties.
     *
     * @return true if delete successfully or false
     */
    public static boolean remove() {
        return FILE.delete();
    }
}
