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

package org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')};

<#if feature=="encrypt" || feature=="shadow">
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.User;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.repository.UserRepository;
<#else>
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.Order;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.OrderItem;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.repository.OrderItemRepository;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.repository.OrderRepository;
</#if>
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

<#assign frameworkName="">
<#list framework?split("-") as framework1>
    <#assign frameworkName=frameworkName + framework1?cap_first>
</#list>
<#assign featureName="">
<#list feature?split("-") as feature1>
    <#assign featureName=featureName + feature1?cap_first>
</#list>
@Service
public final class ${mode?cap_first}${transaction?cap_first}${featureName}${frameworkName}ExampleService {

<#if feature=="encrypt" || feature=="shadow">
    @Resource
    private UserRepository userRepository;
<#else>
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
</#if>

    /**
     * Execute test.
     *
     * @throws SQLException
     */
    public void run() {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
<#if feature=="encrypt" || feature=="shadow">
    <#include "userExampleService.ftl">
<#else>
    <#include "orderExampleService.ftl">
</#if>
}
