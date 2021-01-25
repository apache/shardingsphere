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

package org.apache.shardingsphere.example.sharding.spring.namespace.jpa;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public final class ExampleMain {
    
    private static final String CONFIG_FILE = "META-INF/application-sharding-databases.xml";
//    private static final String CONFIG_FILE = "META-INF/application-sharding-tables.xml";
//    private static final String CONFIG_FILE = "META-INF/application-sharding-databases-tables.xml";
//    private static final String CONFIG_FILE = "META-INF/application-replica-query.xml";
//    private static final String CONFIG_FILE = "META-INF/application-sharding-replica-query.xml";
    
    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_FILE)) {
            ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
        }
    }
}
