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

package org.apache.shardingsphere.example.${feature}.${framework?replace('-', '.')};

<#if framework?contains("springboot")>
<#if framework=="springboot-starter-mybatis">
import org.mybatis.spring.annotation.MapperScan;
<#elseif framework=="springboot-starter-jpa">
import org.springframework.boot.autoconfigure.domain.EntityScan;
</#if>
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
<#elseif framework?contains("spring-namespace")>
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
<#elseif framework=="jdbc">

import javax.sql.DataSource;
</#if>
import java.sql.SQLException;

<#assign frameworkName="">
<#list framework?split("-") as framework1>
    <#assign frameworkName=frameworkName + framework1?cap_first>
</#list>
<#if framework=="springboot-starter-mybatis">
@MapperScan("org.apache.shardingsphere.example.${feature}.springboot.starter.mybatis.repository")
</#if>
<#if framework=="springboot-starter-jpa">
@EntityScan(basePackages = "org.apache.shardingsphere.example.${feature}.springboot.starter.jpa.entity")
</#if>
<#if framework?contains("springboot")>
@SpringBootApplication
</#if>
public class ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${frameworkName}Example {
    
    public static void main(final String[] args) throws SQLException {
    <#if framework=="jdbc">
        ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${framework?cap_first}Configuration shardingConfiguration = new ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${framework?cap_first}Configuration();
        DataSource dataSource = shardingConfiguration.getDataSource();
        ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${framework?cap_first}ExampleService exampleService = new ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${framework?cap_first}ExampleService(dataSource);
        exampleService.run();
    <#else>
    <#if framework?contains("spring-namespace")>
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml")) {
    <#else>
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(${mode?cap_first}${transaction?cap_first}${feature?cap_first}${frameworkName}Example.class, args)) {
    </#if>
            ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${frameworkName}ExampleService exampleService = applicationContext.getBean(${mode?cap_first}${transaction?cap_first}${feature?cap_first}${frameworkName}ExampleService.class);
            exampleService.run();
        }
    </#if>
    }
}
