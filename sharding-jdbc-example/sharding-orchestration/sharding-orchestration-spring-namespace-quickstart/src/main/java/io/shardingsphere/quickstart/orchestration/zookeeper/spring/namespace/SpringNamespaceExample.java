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

package io.shardingsphere.quickstart.orchestration.zookeeper.spring.namespace;

import io.shardingsphere.quickstart.common.mybatis.service.SpringPojoService;
import io.shardingsphere.quickstart.type.RegistryCenterType;
import io.shardingsphere.quickstart.type.ShardingType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringNamespaceExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ETCD;
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(getApplicationFile())) {
            SpringPojoService commonService = applicationContext.getBean(SpringPojoService.class);
            commonService.initEnvironment();
            commonService.processSuccess();
            commonService.cleanEnvironment();
        }
    }
    
    private static String getApplicationFile() {
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return String.format("META-INF/%s/%s/application-sharding-databases-tables.xml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
            case MASTER_SLAVE:
                return String.format("META-INF/%s/%s/application-master-slave.xml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
}
