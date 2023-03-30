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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Proxy example generator.
 */
public final class ProxyExampleGenerator implements ExampleGenerator {
    
    @Override
    public void generate(final Configuration templateConfig, final Map<String, String> dataModel, final String relativePath) throws IOException, TemplateException {
        GenerateUtils.generateDirs(templateConfig, dataModel, Collections.singleton("conf"), relativePath + RESOURCES_PATH);
        String outputPath = GenerateUtils.generatePath(templateConfig, dataModel, relativePath);
        processFile(templateConfig, dataModel, outputPath);
    }
    
    private void processFile(final Configuration templateConfig, final Map<String, String> dataModel,
                             final String baseOutputPath) throws TemplateException, IOException {
        String outputPath = baseOutputPath + RESOURCES_PATH + "/conf/";
        GenerateUtils.processFile(templateConfig, dataModel, getType() + "/config-example_db.ftl", outputPath + "config-example_db.yaml");
        GenerateUtils.processFile(templateConfig, dataModel, getType() + "/server.ftl", outputPath + "server.yaml");
        GenerateUtils.processFile(templateConfig, dataModel, getType() + "/pom.ftl", baseOutputPath + "pom.xml");
    }
    
    public String getType() {
        return "proxy";
    }
}
