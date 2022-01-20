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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Example scenario factory.
 */
public final class ExampleScenarioFactory {
    
    private final FeatureExampleScenario featureScenario;
    
    private final FrameworkExampleScenario frameworkScenario;
    
    public ExampleScenarioFactory(final Map<String, String> dataModel) {
        featureScenario = getFeatureScenario(dataModel.get("feature"));
        frameworkScenario = getFrameworkScenario(dataModel.get("framework"));
    }
    
    private FeatureExampleScenario getFeatureScenario(final String feature) {
        for (FeatureExampleScenario each : ServiceLoader.load(FeatureExampleScenario.class)) {
            if (each.getType().equals(feature)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Can not support example scenario with feature `%s`.", feature));
    }
    
    private FrameworkExampleScenario getFrameworkScenario(final String framework) {
        for (FrameworkExampleScenario each : ServiceLoader.load(FrameworkExampleScenario.class)) {
            if (each.getType().equals(framework)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Can not support example scenario with framework `%s`.", framework));
    }
    
    /**
     * Get java class template map.
     *
     * @return java class template map
     */
    public Map<String, String> getJavaClassTemplateMap() {
        Map<String, String> result = new HashMap<>();
        result.put("java/Example.ftl", "Example.java");
        result.put("java/ExampleService.ftl", "ExampleService.java");
        result.put("java/entity/Order.ftl", "entity/Order.java");
        result.put("java/entity/OrderItem.ftl", "entity/OrderItem.java");
        result.put("java/entity/Address.ftl", "entity/Address.java");
        result.putAll(featureScenario.getJavaClassTemplateMap());
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
        result.putAll(featureScenario.getResourceTemplateMap());
        result.putAll(frameworkScenario.getResourceTemplateMap());
        result.put("resources/logback.ftl", "logback.xml");
        return result;
    }
}
