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

package org.apache.shardingsphere.example.generator.scenario;

import org.apache.shardingsphere.example.generator.scenario.feature.FeatureExampleScenario;
import org.apache.shardingsphere.example.generator.scenario.framework.FrameworkExampleScenario;
import org.apache.shardingsphere.example.generator.scenario.transaction.TransactionExampleScenario;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example scenario factory.
 */
public final class ExampleScenarioFactory {
    
    private final Collection<FeatureExampleScenario> featureScenarios;
    
    private final FrameworkExampleScenario frameworkScenario;
    
    private final TransactionExampleScenario transactionScenario;
    
    public ExampleScenarioFactory(final String feature, final String framework, final String transaction) {
        featureScenarios = getFeatureScenarios(feature);
        frameworkScenario = getFrameworkScenario(framework);
        transactionScenario = getTransactionScenario(transaction);
    }
    
    private Collection<FeatureExampleScenario> getFeatureScenarios(final String feature) {
        return null == feature
                ? Collections.emptyList()
                : Arrays.stream(feature.split(",")).map(each -> TypedSPILoader.getService(FeatureExampleScenario.class, each.trim())).collect(Collectors.toList());
    }
    
    private FrameworkExampleScenario getFrameworkScenario(final String framework) {
        return TypedSPILoader.getService(FrameworkExampleScenario.class, framework);
    }
    
    private TransactionExampleScenario getTransactionScenario(final String transaction) {
        return TypedSPILoader.getService(TransactionExampleScenario.class, transaction);
    }
    
    /**
     * Get java class template map.
     *
     * @return java class template map
     */
    public Map<String, String> getJavaClassTemplateMap() {
        Map<String, String> result = new HashMap<>();
        result.put("java/service/ExampleService.ftl", "service/ExampleService.java");
        result.put("java/entity/Order.ftl", "entity/Order.java");
        result.put("java/entity/OrderItem.ftl", "entity/OrderItem.java");
        result.put("java/entity/Address.ftl", "entity/Address.java");
        for (FeatureExampleScenario each : featureScenarios) {
            result.putAll(each.getJavaClassTemplateMap());
        }
        if (frameworkScenario.getType().contains("spring-boot-starter") && (transactionScenario.getType().contains("xa"))) {
            result.put("java/TransactionConfiguration.ftl", "TransactionConfiguration.java");
        }
        result.putAll(frameworkScenario.getJavaClassTemplateMap());
        return result;
    }
    
    /**
     * Get resource template map.
     *
     * @return resource template map
     */
    public Map<String, String> getResourceTemplateMap() {
        Map<String, String> result = new HashMap<>();
        for (FeatureExampleScenario each : featureScenarios) {
            result.putAll(each.getResourceTemplateMap());
        }
        result.putAll(frameworkScenario.getResourceTemplateMap());
        result.putAll(transactionScenario.getResourceTemplateMap());
        result.put("resources/logback.ftl", "logback.xml");
        return result;
    }
    
    /**
     * Get java class paths.
     *
     * @return java class paths
     */
    public Collection<String> getJavaClassPaths() {
        Collection<String> result = new HashSet<>();
        for (FeatureExampleScenario each : featureScenarios) {
            result.addAll(each.getJavaClassPaths());
        }
        result.addAll(frameworkScenario.getJavaClassPaths());
        result.add("entity");
        result.add("service");
        return result;
    }
    
    /**
     * Get resource paths.
     *
     * @return resource paths
     */
    public Collection<String> getResourcePaths() {
        Collection<String> result = new HashSet<>();
        for (FeatureExampleScenario each : featureScenarios) {
            result.addAll(each.getResourcePaths());
        }
        result.addAll(frameworkScenario.getResourcePaths());
        return result;
    }
}
