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

package org.apache.shardingsphere.example.generator;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.shardingsphere.example.generator.scenario.ExampleScenarioFactory;

import java.io.IOException;
import java.util.Map;

/**
 * JDBC example generator.
 */
public final class JDBCExampleGenerator implements ExampleGenerator {
    
    private static final String JAVA_CLASS_PATH = "src/main/java/org/apache/shardingsphere/example/"
            + "<#assign package = feature?replace('-', '.')?replace(',', '.') />"
            + "${package}/${framework?replace('-', '/')}";
    
    @Override
    public void generate(final Configuration templateConfig, final Map<String, String> dataModel) throws IOException, TemplateException {
        String features = dataModel.get("features");
        String frameworks = dataModel.get("frameworks");
        for (String eachFramework : frameworks.split(",")) {
            for (String eachFeature : GenerateUtil.generateCombination(features.split(","))) {
                dataModel.put("feature", eachFeature);
                dataModel.put("framework", eachFramework);
                GenerateUtil.generateDirs(templateConfig, dataModel, new ExampleScenarioFactory(eachFeature, eachFramework).getJavaClassPaths(), OUTPUT_PATH + JAVA_CLASS_PATH);
                GenerateUtil.generateDirs(templateConfig, dataModel, new ExampleScenarioFactory(eachFeature, eachFramework).getResourcePaths(), OUTPUT_PATH + RESOURCES_PATH);
                GenerateUtil.generateFile(templateConfig, getType(), dataModel, new ExampleScenarioFactory(eachFeature, eachFramework).getJavaClassTemplateMap(), OUTPUT_PATH + JAVA_CLASS_PATH);
                GenerateUtil.generateFile(templateConfig, getType(), dataModel, new ExampleScenarioFactory(eachFeature, eachFramework).getResourceTemplateMap(), OUTPUT_PATH + RESOURCES_PATH);
                String outputPath = GenerateUtil.generatePath(templateConfig, dataModel, OUTPUT_PATH);
                GenerateUtil.processFile(templateConfig, dataModel, getType() + "/pom.ftl", outputPath + "pom.xml");
            }
        }
    }
    
    @Override
    public String getType() {
        return "jdbc";
    }
}
