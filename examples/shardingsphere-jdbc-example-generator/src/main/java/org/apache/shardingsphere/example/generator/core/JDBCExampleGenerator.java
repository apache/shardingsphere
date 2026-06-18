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
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfigurationValidator;
import org.apache.shardingsphere.example.generator.scenario.ExampleScenarioFactory;
import org.apache.shardingsphere.infra.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * JDBC example generator.
 */
public final class JDBCExampleGenerator {
    
    private static final String CONFIG_FILE = "/config.yaml";
    
    private static final String JAVA_CLASS_PATH = "src/main/java/org/apache/shardingsphere/example/"
            + "<#assign package = feature?replace('-', '')?replace(',', '.') />"
            + "${package}/${framework?replace('-', '/')}";
    
    private static final String PROJECT_PATH = "shardingsphere-jdbc-sample/${feature?replace(',', '-')}--${framework}--${mode}--${transaction}/";
    
    private static final String RESOURCES_PATH = "src/main/resources";
    
    private final Configuration templateConfig;
    
    public JDBCExampleGenerator() throws IOException {
        templateConfig = createTemplateConfiguration();
    }
    
    private Configuration createTemplateConfiguration() throws IOException {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("template")).getFile()));
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Generate example.
     * 
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    public void generate() throws IOException, TemplateException {
        YamlExampleConfiguration exampleConfig = buildExampleConfiguration();
        YamlExampleConfigurationValidator.validate(exampleConfig);
        String outputPath = buildOutputPath(exampleConfig);
        for (String eachMode : exampleConfig.getModes()) {
            for (String eachTransaction : exampleConfig.getTransactions()) {
                for (String eachFramework : exampleConfig.getFrameworks()) {
                    for (String eachFeature : GenerateUtils.generateCombination(exampleConfig.getFeatures())) {
                        doGenerate(templateConfig, buildDataModel(exampleConfig.getProps(), eachMode, eachTransaction, eachFramework, eachFeature), outputPath);
                    }
                }
            }
        }
    }
    
    private YamlExampleConfiguration buildExampleConfiguration() {
        YamlExampleConfiguration result = swapConfigToObject();
        Properties props = new Properties();
        for (String each : System.getProperties().stringPropertyNames()) {
            props.setProperty(each, System.getProperty(each));
        }
        if (!props.isEmpty()) {
            if (props.containsKey("output")) {
                result.setOutput(props.getProperty("output"));
            }
            if (props.containsKey("modes")) {
                result.setModes(getSysEnvByKey(props, "modes"));
            }
            
            if (props.containsKey("transactions")) {
                result.setTransactions(getSysEnvByKey(props, "transactions"));
            }
            if (props.containsKey("features")) {
                result.setFeatures(getSysEnvByKey(props, "features"));
            }
            
            if (props.containsKey("frameworks")) {
                result.setFrameworks(getSysEnvByKey(props, "frameworks"));
            }
        }
        return result;
    }
    
    private List<String> getSysEnvByKey(final Properties props, final String key) {
        return Lists.newArrayList(props.getProperty(key).split(","));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlExampleConfiguration swapConfigToObject() {
        URL url = JDBCExampleGenerator.class.getResource(CONFIG_FILE);
        File file = null == url ? new File(CONFIG_FILE) : new File(url.toURI().getPath());
        return YamlEngine.unmarshal(file, YamlExampleConfiguration.class);
    }
    
    /**
     * Build output path.
     * 
     * @param exampleConfig example configuration
     * @return built output path
     */
    private String buildOutputPath(final YamlExampleConfiguration exampleConfig) {
        if (Strings.isNullOrEmpty(exampleConfig.getOutput())) {
            File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("")).getPath());
            return file.getParent() + "/generated-sources/" + PROJECT_PATH;
        }
        return exampleConfig.getOutput() + PROJECT_PATH;
    }
    
    private void doGenerate(final Configuration templateConfig, final Map<String, String> dataModel, final String relativePath) throws IOException, TemplateException {
        ExampleScenarioFactory exampleScenarioFactory = new ExampleScenarioFactory(dataModel.get("feature"), dataModel.get("framework"), dataModel.get("transaction"));
        GenerateUtils.generateDirs(templateConfig, dataModel, exampleScenarioFactory.getJavaClassPaths(), relativePath + JAVA_CLASS_PATH);
        GenerateUtils.generateDirs(templateConfig, dataModel, exampleScenarioFactory.getResourcePaths(), relativePath + RESOURCES_PATH);
        GenerateUtils.generateFile(templateConfig, "", dataModel, exampleScenarioFactory.getJavaClassTemplateMap(), relativePath + JAVA_CLASS_PATH);
        GenerateUtils.generateFile(templateConfig, "", dataModel, exampleScenarioFactory.getResourceTemplateMap(), relativePath + RESOURCES_PATH);
        String outputPath = GenerateUtils.generatePath(templateConfig, dataModel, relativePath);
        GenerateUtils.processFile(templateConfig, dataModel, "/pom.ftl", outputPath + "pom.xml");
        GenerateUtils.processFile(templateConfig, dataModel, "/init.ftl", outputPath + "init.sql");
    }
    
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
    private Map<String, String> buildDataModel(final Properties props, final String mode, final String transaction, final String framework, final String feature) {
        Map<String, String> result = new LinkedHashMap<>(props.size() + 5, 1F);
        props.forEach((key, value) -> result.put(key.toString(), value.toString()));
        result.put("mode", mode);
        result.put("transaction", transaction);
        result.put("feature", feature);
        result.put("framework", framework);
        result.put("shardingsphereVersion", ShardingSphereVersion.VERSION);
        result.put("namespace", buildNamespace(feature, framework, mode, transaction));
        return result;
    }
    
    private String buildNamespace(final String feature, final String framework, final String mode, final String transaction) {
        return String.format("generator-%s-%s-%s-%s", sanitizeSegment(feature), sanitizeSegment(framework), sanitizeSegment(mode), sanitizeSegment(transaction));
    }
    
    private String sanitizeSegment(final String value) {
        return value.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "-");
    }
}
