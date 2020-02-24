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

package org.apache.shardingsphere.example.orchestration.spring.namespace;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.type.RegistryCenterType;
import org.apache.shardingsphere.example.type.ShardingType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public class ExampleMain {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.ENCRYPT;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
    private static RegistryCenterType registryCenterType = RegistryCenterType.NACOS;

    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(getApplicationFile())) {
            ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
        }
    }
    
    private static String getApplicationFile() {
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return String.format("META-INF/%s/%s/application-sharding-databases-tables.xml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
            case MASTER_SLAVE:
                return String.format("META-INF/%s/%s/application-master-slave.xml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
            case ENCRYPT:
                return String.format("META-INF/%s/%s/application-encrypt.xml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
}
