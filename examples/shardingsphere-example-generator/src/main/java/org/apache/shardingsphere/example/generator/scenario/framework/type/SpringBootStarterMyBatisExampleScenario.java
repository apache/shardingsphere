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

package org.apache.shardingsphere.example.generator.scenario.framework.type;

import org.apache.shardingsphere.example.generator.scenario.framework.FrameworkExampleScenario;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring-Boot starter MyBatis example scenario.
 */
public final class SpringBootStarterMyBatisExampleScenario implements FrameworkExampleScenario {
    
    @Override
    public Map<String, String> getJavaClassTemplateMap() {
        Map<String, String> result = new HashMap<>(4, 1);
        result.put("java/main/SpringBootStarMyBatisExampleMain.ftl", "ExampleMain.java");
        result.put("java/repository/mybatis/OrderItemRepository.ftl", "repository/OrderItemRepository.java");
        result.put("java/repository/mybatis/OrderRepository.ftl", "repository/OrderRepository.java");
        result.put("java/repository/mybatis/AddressRepository.ftl", "repository/AddressRepository.java");
        return result;
    }
    
    @Override
    public Map<String, String> getResourceTemplateMap() {
        Map<String, String> result = new HashMap<>(5, 1);
        result.put("resources/properties/application.ftl", "application.properties");
        result.put("resources/yaml/config.ftl", "config.yaml");
        result.put("resources/mappers/OrderItemMapper.ftl", "mappers/OrderItemMapper.xml");
        result.put("resources/mappers/OrderMapper.ftl", "mappers/OrderMapper.xml");
        result.put("resources/mappers/AddressMapper.ftl", "mappers/AddressMapper.xml");
        return result;
    }
    
    @Override
    public Collection<String> getJavaClassPaths() {
        return Collections.singleton("repository");
    }
    
    @Override
    public Collection<String> getResourcePaths() {
        return Collections.singleton("mappers");
    }
    
    @Override
    public String getType() {
        return "spring-boot-starter-mybatis";
    }
}
