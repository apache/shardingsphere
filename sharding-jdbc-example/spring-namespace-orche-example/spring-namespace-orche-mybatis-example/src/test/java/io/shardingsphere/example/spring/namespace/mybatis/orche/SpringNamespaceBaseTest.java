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

package io.shardingsphere.example.spring.namespace.mybatis.orche;

import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.AbstractOrchestrationDataSource;
import org.junit.AfterClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Method;

public abstract class SpringNamespaceBaseTest {
    
    private static DataSource staticDataSource;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @PostConstruct
    public void beforeInit() {
        staticDataSource = configurableApplicationContext.getBean(AbstractOrchestrationDataSource.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        closeDataSource(staticDataSource);
    }

    private static void closeDataSource(final DataSource dataSource) throws Exception {
        Method method = dataSource.getClass().getMethod("close");
        method.setAccessible(true);
        method.invoke(dataSource);
    }
}
