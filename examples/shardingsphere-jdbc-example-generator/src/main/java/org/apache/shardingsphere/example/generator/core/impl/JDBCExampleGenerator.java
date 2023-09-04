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
import org.apache.shardingsphere.example.generator.core.GenerateUtils;
import org.apache.shardingsphere.example.generator.scenario.ExampleScenarioFactory;

import java.io.IOException;
import java.util.Map;

/**
 * JDBC example generator.
 */
public final class JDBCExampleGenerator implements ExampleGenerator {
    
    private static final String JAVA_CLASS_PATH = "src/main/java/org/apache/shardingsphere/example/"
            + "<#assign package = feature?replace('-', '')?replace(',', '.') />"
            + "${package}/${framework?replace('-', '/')}";
    
    @Override
    public void generate(final Configuration templateConfig, final Map<String, String> dataModel, final String relativePath) throws IOException, TemplateException {
        ExampleScenarioFactory exampleScenarioFactory = new ExampleScenarioFactory(dataModel.get("feature"), dataModel.get("framework"), dataModel.get("transaction"));
        GenerateUtils.generateDirs(templateConfig, dataModel, exampleScenarioFactory.getJavaClassPaths(), relativePath + JAVA_CLASS_PATH);
        GenerateUtils.generateDirs(templateConfig, dataModel, exampleScenarioFactory.getResourcePaths(), relativePath + RESOURCES_PATH);
        GenerateUtils.generateFile(templateConfig, "", dataModel, exampleScenarioFactory.getJavaClassTemplateMap(), relativePath + JAVA_CLASS_PATH);
        GenerateUtils.generateFile(templateConfig, "", dataModel, exampleScenarioFactory.getResourceTemplateMap(), relativePath + RESOURCES_PATH);
        String outputPath = GenerateUtils.generatePath(templateConfig, dataModel, relativePath);
        GenerateUtils.processFile(templateConfig, dataModel, "/pom.ftl", outputPath + "pom.xml");
    }
    
    @Override
    public String getType() {
        return "jdbc";
    }
}
