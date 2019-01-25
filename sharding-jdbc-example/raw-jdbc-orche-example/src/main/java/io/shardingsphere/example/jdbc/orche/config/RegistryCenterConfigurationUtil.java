/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.orche.config;

import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;

public class RegistryCenterConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NAMESPACE = "orchestration-java-demo";
    
    private static final String ETCD_CONNECTION_STRING = "http://localhost:2379";
    
    public static RegistryCenterConfiguration getZooKeeperConfiguration() {
        RegistryCenterConfiguration result = new RegistryCenterConfiguration();
        result.setServerLists(ZOOKEEPER_CONNECTION_STRING);
        result.setNamespace(NAMESPACE);
        return result;
    }
    
    public static RegistryCenterConfiguration getEtcdConfiguration() {
        RegistryCenterConfiguration result = new RegistryCenterConfiguration();
        result.setServerLists(ETCD_CONNECTION_STRING);
        return result;
    }
}
