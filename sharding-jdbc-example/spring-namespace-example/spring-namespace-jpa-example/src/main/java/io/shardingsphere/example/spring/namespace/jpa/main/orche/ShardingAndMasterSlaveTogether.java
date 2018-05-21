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

package io.shardingsphere.example.spring.namespace.jpa.main.orche;

import io.shardingsphere.example.spring.namespace.jpa.service.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class ShardingAndMasterSlaveTogether {
    
    private static final boolean LOAD_CONFIG_FROM_REG_CENTER = false;
    
    private static final boolean REG_CENTER_IS_ETCD = false;
    
    public static void main(final String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(getConfigFileName("shardingMasterSlave"));
        DemoService demoService = applicationContext.getBean(DemoService.class);
        demoService.demo();
        applicationContext.close();
    }
    
    private static String getConfigFileName(final String configType) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("META-INF/orche/");
        if (REG_CENTER_IS_ETCD) {
            stringBuilder.append("etcd/");
        } else {
            stringBuilder.append("zookeeper/");
        }
        if (LOAD_CONFIG_FROM_REG_CENTER) {
            stringBuilder.append("cloud/");
        } else {
            stringBuilder.append("local/");
        }
        stringBuilder.append(configType);
        stringBuilder.append(".xml");
        return stringBuilder.toString();
    }
}
