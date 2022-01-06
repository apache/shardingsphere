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

package org.apache.shardingsphere.example.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Example template resource factory.
 */
public final class ExampleTemplateFactory {
    
    private static final String FEATURE_KEY = "feature";
    
    private static final String FRAMEWORK_KEY = "framework";

    /**
     * Get the template resource that needs to be renamed
     * @param dataModel data model
     * @return rename template map
     */
    public static Map<String, String> getRenameTemplate(Map<String, String> dataModel) {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put("Example", "java/Example.ftl");
        result.put("ExampleService", "java/ExampleService.ftl");
        return result;
    }
    
    /**
     * Get template resources that do not need to be renamed
     * @param dataModel data model
     * @return not need rename map
     */
    public static Map<String, String> getUnReNameTemplate(Map<String, String> dataModel) {
        String feature = dataModel.get(FEATURE_KEY);
        Map<String, String> result = new HashMap<>(8, 1);
        if (FeatureType.ENCRYPT.getFeature().equals(feature)) {
            result.put("TestQueryAssistedShardingEncryptAlgorithm", "TestQueryAssistedShardingEncryptAlgorithm.java");
        }
        result.put("java/entity/Order", "entity/Order.java");
        result.put("java/entity/OrderItem", "entity/OrderItem.java");
        result.put("java/repository/OrderItemRepository", "repository/OrderItemRepository.ftl");
        result.put("java/repository/OrderRepository", "repository/OrderRepository.java");
        return result;
    }
    
    /**
     * Get template resources map
     * @param dataModel data model
     * @return resource map
     */
    public static Map<String, String> getResourceTemplate(Map<String, String> dataModel) {
        String feature = dataModel.get(FEATURE_KEY);
        String framework = dataModel.get(FRAMEWORK_KEY);
        Map<String, String> result = new HashMap<>(8, 1);
        result.put("log/logback", "logback.xml");
        if (FeatureType.ENCRYPT.getFeature().equals(feature)) {
            result.put("spi/encryptAlgorithm", "META-INF/services/org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm");
        }
        if (framework.contains("spring-boot-starter")) {
            result.put("properties/application", "application.properties");
        } else if (framework.contains("spring-namespace")) {
            result.put("xml/application", "application.xml");
        }
        if (framework.contains("mybatis")) {
            result.put("mybatis/mappers/OrderItemMapper", "mappers/OrderItemMapper.xml");
            result.put("mybatis/mappers/OrderMapper", "mappers/OrderMapper.xml");
        }
        return result;
    }
}
