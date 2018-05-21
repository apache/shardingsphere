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

package io.shardingsphere.example.spring.namespace.jpa.main.nodep;

import io.shardingsphere.example.spring.namespace.jpa.fixture.service.DemoService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ShardingOnlyWithTables {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/nodep/shardingDatabases.xml")) {
            DemoService demoService = applicationContext.getBean(DemoService.class);
            demoService.demo();
        }
    }
}
