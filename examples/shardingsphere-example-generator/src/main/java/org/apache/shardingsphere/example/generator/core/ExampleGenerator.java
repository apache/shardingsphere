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

package org.apache.shardingsphere.example.generator.core;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Example generator.
 */
public interface ExampleGenerator extends TypedSPI {
    
    String OUTPUT_PATH = "./examples/shardingsphere-example-generator/target/generated-sources/shardingsphere-${product}-sample/${feature?replace(',', '-')}--${framework}--${mode}--${transaction}/";
    
    String RESOURCES_PATH = "src/main/resources";
    
    default void generate(final Configuration templateConfig, final YamlExampleConfiguration exampleConfig) throws IOException, TemplateException {
        for (String eachMode : exampleConfig.getModes()) {
            for (String eachTransaction : exampleConfig.getTransactions()) {
                for (String eachFramework : exampleConfig.getFrameworks()) {
                    for (String eachFeature : GenerateUtil.generateCombination(exampleConfig.getFeatures())) {
                        generate(templateConfig, buildDataModel(exampleConfig.getProps(), eachMode, eachTransaction, eachFramework, eachFeature), eachFeature, eachFramework, eachTransaction);
                    }
                }
            }
        }
    }

    default Map<String, String> buildDataModel(final Properties props, final String mode, final String transaction, final String framework, final String feature) {
        Map<String, String> result = new LinkedHashMap<>();
        props.forEach((key, value) -> result.put(key.toString(), value.toString()));
        result.put("product", getType());
        result.put("mode", mode);
        result.put("transaction", transaction);
        result.put("feature", feature);
        result.put("framework", framework);
        result.put("shardingsphereVersion", ShardingSphereVersion.VERSION);
        return result;
    }
    
    /**
     * Generate.
     * @param templateConfig template configuration
     * @param dataModel data model
     * @param framework framework
     * @param feature feature
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    void generate(final Configuration templateConfig, final Map<String, String> dataModel, final String framework, final String feature, String transaction) throws IOException, TemplateException;
}
