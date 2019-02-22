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

package io.shardingsphere.example.spring.namespace.mybatis.nodep;

import io.shardingsphere.example.repository.api.senario.AnnotationCommonServiceScenario;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.mybatis.service.SpringPojoService;
import io.shardingsphere.example.type.ShardingType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringNamespaceExample {
    
    private static ShardingType type = ShardingType.SHARDING_DATABASES;
//    private static ShardingType type = ShardingType.SHARDING_TABLES;
//    private static ShardingType type = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType type = ShardingType.MASTER_SLAVE;
//    private static ShardingType type = ShardingType.SHARDING_MASTER_SLAVE;
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(getApplicationFilePrecise())) {
            process(applicationContext);
        }
    }
    
    private static String getApplicationFilePrecise() {
        switch (type) {
            case SHARDING_DATABASES:
                return "META-INF/application-sharding-databases.xml";
            case SHARDING_TABLES:
                return "META-INF/application-sharding-tables.xml";
            case SHARDING_DATABASES_AND_TABLES:
                return "META-INF/application-sharding-databases-tables.xml";
            case MASTER_SLAVE:
                return "META-INF/application-master-slave.xml";
            case SHARDING_MASTER_SLAVE:
                return "META-INF/application-sharding-master-slave.xml";
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) {
        CommonService commonService = getCommonService(applicationContext);
        AnnotationCommonServiceScenario scenario = new AnnotationCommonServiceScenario(commonService);
        scenario.process();
    }
    
    private static CommonService getCommonService(final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBean(SpringPojoService.class);
    }
}
