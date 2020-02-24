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

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config;

import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;

import java.util.Properties;

public class RegistryCenterConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NAMESPACE = "orchestration-java-demo";
    
    private static final String NACOS_CONNECTION_STRING = "localhost:8848";

    private static final String NACOS_NAMESPACE = "";
    
    public static RegistryCenterConfiguration getZooKeeperConfiguration() {
        RegistryCenterConfiguration result = new RegistryCenterConfiguration("zookeeper");
        result.setServerLists(ZOOKEEPER_CONNECTION_STRING);
        result.setNamespace(NAMESPACE);
        return result;
    }
    
    public static RegistryCenterConfiguration getNacosConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
        properties.setProperty("timeout", "3000");
        RegistryCenterConfiguration result = new RegistryCenterConfiguration("nacos", properties);
        result.setServerLists(NACOS_CONNECTION_STRING);
        result.setNamespace(NACOS_NAMESPACE);
        return result;
    }
}
