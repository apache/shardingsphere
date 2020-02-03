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

package org.apache.shardingsphere.orchestration.reg.nacos;

import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

public final class NacosRegistryCenterTest {

    private static RegistryCenter nacosRegistryCenter = new NacosRegistryCenter();

    @BeforeClass
    public static void init() {
        Properties properties = new Properties();
        properties.setProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
        properties.setProperty("timeout", "3000");
        RegistryCenterConfiguration configuration = new RegistryCenterConfiguration(nacosRegistryCenter.getType(), properties);
        configuration.setServerLists("127.0.0.1:8848");
        nacosRegistryCenter.init(configuration);
    }

    @Test
    public void assertPersist() {
        nacosRegistryCenter.persist("sharding/test", "value1");
    }

    @Test
    public void assertUpdate() {
        nacosRegistryCenter.update("sharding/test", "value2");
    }
}
