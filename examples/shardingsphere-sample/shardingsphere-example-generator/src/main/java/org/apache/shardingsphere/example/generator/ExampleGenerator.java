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
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Example generator.
 */
public final class ExampleGenerator {
    
    private static final String DATA_MODEL_PATH = "/data-model/data-model.yaml";
    
    private static final String OUTPUT_PATH = "./examples/shardingsphere-sample/shardingsphere-example-generated"
            + "<#assign package=\"\">"
            + "<#if feature?split(\",\")?size gt 1>"
            + "<#assign package=\"mixed\">"
            + "<#else>"
            + "<#assign package=feature />"
            + "</#if>"
            + "/shardingsphere-jdbc-${mode}-${transaction}-${package}-${framework}-example/src/main/";
    
    private static final String JAVA_CLASS_PATH = "java/org/apache/shardingsphere/example/"
            + "<#assign package=\"\">"
            + "<#if feature?split(\",\")?size gt 1>"
            + "<#assign package=\"mixed\">"
            + "<#else>"
            + "<#assign package=feature?replace('-', '/') />"
            + "</#if>"
            + "${package}/${framework?replace('-', '/')}";
    
    private static final String RESOURCES_PATH = "resources";
    
    private final Configuration templateConfig;
    
    public ExampleGenerator() throws IOException {
        templateConfig = createTemplateConfiguration();
    }
    
    private Configuration createTemplateConfiguration() throws IOException {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(ExampleGenerator.class.getClassLoader().getResource("template")).getFile()));
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Generate file.
     * 
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    @SuppressWarnings("unchecked")
    public void generate() throws IOException, TemplateException {
        try (InputStream input = ExampleGenerator.class.getResourceAsStream(DATA_MODEL_PATH)) {
            Map<String, String> dataModel = new Yaml().loadAs(input, Map.class);
            generateFile(dataModel, ExampleTemplateFactory.getJavaClassTemplateMap(dataModel), JAVA_CLASS_PATH);
            generateFile(dataModel, ExampleTemplateFactory.getResourceTemplateMap(dataModel), RESOURCES_PATH);
        }
    }
    
    private void generateFile(final Map<String, String> dataModel, final Map<String, String> templateMap, final String outputRelativePath) throws IOException, TemplateException {
        String outputPath = generatePath(dataModel, OUTPUT_PATH + outputRelativePath);
        for (Entry<String, String> entry : templateMap.entrySet()) {
            processFile(dataModel, entry.getKey(), outputPath + "/" + entry.getValue());
        }
    }
    
    private String generatePath(final Object model, final String relativePath) throws IOException, TemplateException {
        try (StringWriter result = new StringWriter()) {
            new Template("path", relativePath, templateConfig).process(model, result);
            return result.toString();
        }
    }
    
    private void processFile(final Object model, final String templateFile, final String outputFile) throws IOException, TemplateException {
        try (Writer writer = new FileWriter(outputFile)) {
            templateConfig.getTemplate(templateFile).process(model, writer);
        }
    }
}
