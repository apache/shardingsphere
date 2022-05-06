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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Example scenario factory.
 */
public final class ExampleScenarioFactory {
    
    private final Collection<FeatureExampleScenario> featureScenarios;
    
    private final FrameworkExampleScenario frameworkScenario;
    
    public ExampleScenarioFactory(final String feature, final String framework) {
        featureScenarios = getFeatureScenarios(feature);
        frameworkScenario = getFrameworkScenario(framework);
    }
    
    private Collection<FeatureExampleScenario> getFeatureScenarios(final String feature) {
        Collection<FeatureExampleScenario> result = new LinkedList<>();
        if (null == feature) {
            return result;
        }
        for (FeatureExampleScenario each : ServiceLoader.load(FeatureExampleScenario.class)) {
            if (feature.contains(each.getType())) {
                result.add(each);
            }
        }
        return result;
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
        result.put("java/service/ExampleService.ftl", "service/ExampleService.java");
        result.put("java/entity/Order.ftl", "entity/Order.java");
        result.put("java/entity/OrderItem.ftl", "entity/OrderItem.java");
        result.put("java/entity/Address.ftl", "entity/Address.java");
        for (FeatureExampleScenario each : featureScenarios) {
            result.putAll(each.getJavaClassTemplateMap());
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
