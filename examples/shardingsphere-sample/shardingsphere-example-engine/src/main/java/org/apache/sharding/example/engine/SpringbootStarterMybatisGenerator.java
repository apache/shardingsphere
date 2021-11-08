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

package org.apache.sharding.example.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * spring boot starter mybatis generator.
 */
public final class SpringbootStarterMybatisGenerator extends ExampleGenerateEngine {
    
    private static final Map<String, String> RENAME_TEMPLATE_MAP = new HashMap<>();
    private static final Map<String, String> UN_NAME_TEMPLATE_MAP = new HashMap<>();
    private static final Map<String, String> RESOURCE_TEMPLATE_MAP = new HashMap<>();

    static {
        RENAME_TEMPLATE_MAP.put("Example", "Example.ftl");
        RENAME_TEMPLATE_MAP.put("ExampleService", "ExampleService.ftl");
        
        UN_NAME_TEMPLATE_MAP.put("entity/Order", "entity/Order.java");
        UN_NAME_TEMPLATE_MAP.put("entity/OrderItem", "entity/OrderItem.java");
        UN_NAME_TEMPLATE_MAP.put("mybatis/OrderItemRepository", "repository/OrderItemRepository.java");
        UN_NAME_TEMPLATE_MAP.put("mybatis/OrderRepository", "repository/OrderRepository.java");

        RESOURCE_TEMPLATE_MAP.put("mappers/OrderItemMapper", "mappers/OrderItemMapper.xml");
        RESOURCE_TEMPLATE_MAP.put("mappers/OrderMapper", "mappers/OrderItemMapper.xml");
    }

    public SpringbootStarterMybatisGenerator() {
        super(RENAME_TEMPLATE_MAP, UN_NAME_TEMPLATE_MAP, RESOURCE_TEMPLATE_MAP);
    }
    
    @Override
    protected String getGenerator() {
        return "springboot-starter-mybatis";
    }
    
    public static void main(String[] args) {
        SpringbootStarterMybatisGenerator generator = new SpringbootStarterMybatisGenerator();
        generator.exec();
    }
}
