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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy example generator.
 */
public final class ProxyExampleGenerator implements ExampleGenerator {
    
    private static final String OUTPUT_PATH = "./examples/shardingsphere-sample/shardingsphere-example-generator/target/shardingsphere-example-generated"
            + "/shardingsphere-${product}-sample/${feature?replace(',', '-')}--${framework}--${mode}--${transaction}/";
    
    private static final String RESOURCES_PATH = "src/main/resources";
    
    @Override
    public void generate(final Configuration templateConfig, final Map<String, String> dataModel) throws IOException, TemplateException {
        String features = dataModel.get("features");
        String frameworks = dataModel.get("frameworks");
        for (String eachFramework : frameworks.split(",")) {
            for (String eachFeature : GenerateUtil.generateCombination(features.split(","))) {
                dataModel.put("feature", eachFeature);
                dataModel.put("framework", eachFramework);
                GenerateUtil.generateDirs(templateConfig, dataModel, Collections.singleton("conf"), OUTPUT_PATH + RESOURCES_PATH);
                GenerateUtil.generateFile(templateConfig, getType(), dataModel, buildResourceMap(), OUTPUT_PATH + RESOURCES_PATH);
                String outputPath = GenerateUtil.generatePath(templateConfig, dataModel, OUTPUT_PATH);
                GenerateUtil.processFile(templateConfig, dataModel, "pom.ftl", outputPath + "pom.xml");
            }
        }
    }
    
    private Map<String, String> buildResourceMap() {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put("config.ftl", "config.yaml");
        result.put("server.ftl", "server.yaml");
        return result;
    }
    
    @Override
    public String getType() {
        return "proxy";
    }
}
