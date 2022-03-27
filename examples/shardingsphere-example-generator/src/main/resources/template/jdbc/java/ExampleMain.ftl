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

<#assign package = feature?replace('-', '')?replace(',', '.') />
package org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')};

<#if framework=="jdbc">
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.config.Configuration;
</#if>
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.service.ExampleService;

<#if framework?contains("spring-boot")>
<#if framework=="spring-boot-starter-mybatis">
import org.mybatis.spring.annotation.MapperScan;
<#elseif framework=="spring-boot-starter-jpa">
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

<#assign frameworkName="" />
<#list framework?split("-") as item>
    <#assign frameworkName=frameworkName + item?cap_first />
</#list>
<#assign featureName="" />
<#if feature?split(",")?size gt 1>
    <#assign featureName="Mixed" />
<#else>
    <#list feature?split("-") as item>
        <#assign featureName=featureName + item?cap_first />
    </#list>
</#if>
<#if framework=="spring-boot-starter-mybatis">
@MapperScan("org.apache.shardingsphere.example.${package}.spring.boot.starter.mybatis.repository")
</#if>
<#if framework=="spring-boot-starter-jpa">
@EntityScan(basePackages = "org.apache.shardingsphere.example.${package}.spring.boot.starter.jpa.entity")
</#if>
<#if framework?contains("spring-boot")>
@SpringBootApplication
</#if>
public class ExampleMain {
    
    public static void main(final String[] args) throws SQLException {
    <#if framework=="jdbc">
        Configuration config = new Configuration();
        DataSource dataSource = config.createDataSource();
        ExampleService exampleService = new ExampleService(dataSource);
        exampleService.run();
    <#else>
    <#if framework?contains("spring-namespace")>
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml")) {
    <#else>
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(ExampleMain.class, args)) {
    </#if>
            ExampleService exampleService = applicationContext.getBean(ExampleService.class);
            exampleService.run();
        }
    </#if>
    }
}
