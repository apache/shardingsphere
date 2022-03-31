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

package org.apache.shardingsphere.example.generator.core.impl;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.shardingsphere.example.generator.core.ExampleGenerator;
import org.apache.shardingsphere.example.generator.core.GenerateUtil;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.example.generator.scenario.ExampleScenarioFactory;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JDBC example generator.
 */
public final class JDBCExampleGenerator implements ExampleGenerator {
    
    private static final String JAVA_CLASS_PATH = "src/main/java/org/apache/shardingsphere/example/"
            + "<#assign package = feature?replace('-', '')?replace(',', '.') />"
            + "${package}/${framework?replace('-', '/')}";
    
    @Override
    public void generate(final Configuration templateConfig, final YamlExampleConfiguration configuration) throws IOException, TemplateException {
        for (String eachFramework : configuration.getFrameworks()) {
            for (String eachFeature : GenerateUtil.generateCombination(configuration.getFeatures())) {
                generate(templateConfig, buildDataModel(configuration, eachFramework, eachFeature), eachFramework, eachFeature);
            }
        }
    }
    
    private Map<String, String> buildDataModel(final YamlExampleConfiguration configuration, final String framework, final String feature) {
        Map<String, String> result = new LinkedHashMap<>();
        configuration.getProps().forEach((key, value) -> result.put(key.toString(), value.toString()));
        result.put("product", getType());
        // TODO support mode & transaction combination
        result.put("mode", configuration.getModes().size() > 0 ? configuration.getModes().get(0) : "");
        result.put("transaction", configuration.getTransactions().size() > 0 ? configuration.getTransactions().get(0) : "");
        result.put("feature", feature);
        result.put("framework", framework);
        result.put("shardingsphereVersion", ShardingSphereVersion.VERSION);
        return result;
    }
    
    private void generate(final Configuration templateConfig, final Map<String, String> dataModel, final String framework, final String feature) throws IOException, TemplateException {
        GenerateUtil.generateDirs(templateConfig, dataModel, new ExampleScenarioFactory(feature, framework).getJavaClassPaths(), OUTPUT_PATH + JAVA_CLASS_PATH);
        GenerateUtil.generateDirs(templateConfig, dataModel, new ExampleScenarioFactory(feature, framework).getResourcePaths(), OUTPUT_PATH + RESOURCES_PATH);
        GenerateUtil.generateFile(templateConfig, getType(), dataModel, new ExampleScenarioFactory(feature, framework).getJavaClassTemplateMap(), OUTPUT_PATH + JAVA_CLASS_PATH);
        GenerateUtil.generateFile(templateConfig, getType(), dataModel, new ExampleScenarioFactory(feature, framework).getResourceTemplateMap(), OUTPUT_PATH + RESOURCES_PATH);
        String outputPath = GenerateUtil.generatePath(templateConfig, dataModel, OUTPUT_PATH);
        GenerateUtil.processFile(templateConfig, dataModel, getType() + "/pom.ftl", outputPath + "pom.xml");
    }
    
    @Override
    public String getType() {
        return "jdbc";
    }
}
