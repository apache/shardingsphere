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

package io.shardingsphere.quickstart.spring.boot.mybatis;

import io.shardingsphere.quickstart.common.mybatis.service.SpringPojoService;
import io.shardingsphere.quickstart.common.service.CommonService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("io.shardingsphere.quickstart.common.mybatis")
@MapperScan(basePackages = "io.shardingsphere.quickstart.common.mybatis.repository")
@SpringBootApplication(exclude = JtaAutoConfiguration.class)
public class SpringBootMybatisMain {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootMybatisMain.class, args)) {
            CommonService commonService = applicationContext.getBean(SpringPojoService.class);
            commonService.initEnvironment();
            commonService.processSuccess();
            commonService.cleanEnvironment();
        }
    }
}
