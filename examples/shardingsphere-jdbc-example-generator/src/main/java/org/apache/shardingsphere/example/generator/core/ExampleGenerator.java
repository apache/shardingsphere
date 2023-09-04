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

import com.google.common.base.Strings;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Example generator.
 */
@SingletonSPI
public interface ExampleGenerator extends TypedSPI {
    
    String PROJECT_PATH = "shardingsphere-${product}-sample/${feature?replace(',', '-')}--${framework}--${mode}--${transaction}/";
    
    String RESOURCES_PATH = "src/main/resources";
    
    /**
     * Build output path.
     * 
     * @param exampleConfig example configuration
     * @return built output path
     */
    default String buildOutputPath(YamlExampleConfiguration exampleConfig) {
        if (Strings.isNullOrEmpty(exampleConfig.getOutput())) {
            File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath());
            return file.getParent() + "/generated-sources/" + PROJECT_PATH;
        }
        return exampleConfig.getOutput() + PROJECT_PATH;
    }
    
    /**
     * Generate example.
     * 
     * @param templateConfig template configuration
     * @param exampleConfig example configuration
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    default void generate(final Configuration templateConfig, final YamlExampleConfiguration exampleConfig) throws IOException, TemplateException {
        String outputPath = buildOutputPath(exampleConfig);
        for (String eachMode : exampleConfig.getModes()) {
            for (String eachTransaction : exampleConfig.getTransactions()) {
                for (String eachFramework : exampleConfig.getFrameworks()) {
                    for (String eachFeature : GenerateUtils.generateCombination(exampleConfig.getFeatures())) {
                        generate(templateConfig, buildDataModel(exampleConfig.getProps(), eachMode, eachTransaction, eachFramework, eachFeature), outputPath);
                    }
                }
            }
        }
    }
    
    /**
     * Generate example.
     *
     * @param templateConfig template configuration
     * @param dataModel data model
     * @param outputPath output path
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    void generate(Configuration templateConfig, Map<String, String> dataModel, String outputPath) throws IOException, TemplateException;
    
    /**
     * Build data model.
     * 
     * @param props properties
     * @param mode mode
     * @param transaction transaction
     * @param framework framework
     * @param feature feature
     * @return built data model
     */
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
    
    @Override
    String getType();
}
