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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;

/**
 * Example generate engine.
 */
public final class ExampleGenerateEngine {
    
    private static final Configuration TEMPLATE_CONFIG = new Configuration(Configuration.VERSION_2_3_31);
    
    private static final String DATA_MODEL_PATH = "/data-model/data-model.yaml";
    
    private static final String OUTPUT_PATH = "./examples/shardingsphere-sample/shardingsphere-jdbc-sample/shardingsphere-jdbc-${mode}-example"
            + "<#assign package=\"\">"
            + "<#if feature?split(\",\")?size gt 1>"
            + "<#assign package=\"mixed\">"
            + "<#else>"
            + "<#assign package=feature />"
            + "</#if>"
            + "/shardingsphere-jdbc-${mode}-${transaction}-example/shardingsphere-jdbc-${mode}-${transaction}-${package}-example"
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
    
    private static Map<String, String> renameTemplateMap;
    
    private static Map<String, String> unRenameTemplateMap;
    
    private static Map<String, String> resourceTemplateMap;
    
    static {
        try {
            TEMPLATE_CONFIG.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(ExampleGenerateEngine.class.getClassLoader().getResource("template")).getFile()));
            TEMPLATE_CONFIG.setDefaultEncoding("UTF-8");
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Generate file.
     * 
     * @param args args
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws IOException, TemplateException {
        try (InputStream input = ExampleGenerateEngine.class.getResourceAsStream(DATA_MODEL_PATH)) {
            Map<String, String> dataModel = new Yaml().loadAs(input, Map.class);
            fillTemplateMap(dataModel);
            generateJavaFiles(dataModel);
            generateResourcesFile(dataModel);
        }
    }
    
    private static void fillTemplateMap(final Map<String, String> dataModel) {
        renameTemplateMap = ExampleTemplateFactory.getRenameTemplate(dataModel);
        unRenameTemplateMap = ExampleTemplateFactory.getUnReNameTemplate(dataModel);
        resourceTemplateMap = ExampleTemplateFactory.getResourceTemplate(dataModel);
    }
    
    private static void generateJavaFiles(final Map<String, String> dataModel) throws IOException, TemplateException {
        String outputPath = processString(dataModel, OUTPUT_PATH + JAVA_CLASS_PATH);
        for (String each : renameTemplateMap.keySet()) {
            processFile(dataModel, each, outputPath + "/" + renameTemplateMap.get(each));
        }
        for (String each : unRenameTemplateMap.keySet()) {
            processFile(dataModel, each, outputPath + "/" + unRenameTemplateMap.get(each));
        }
    }
    
    private static void generateResourcesFile(final Map<String, String> dataModel) throws IOException, TemplateException {
        String outputPath = processString(dataModel, OUTPUT_PATH + RESOURCES_PATH);
        for (String each : resourceTemplateMap.keySet()) {
            processFile(dataModel, each, outputPath + "/" + resourceTemplateMap.get(each));
        }
    }
    
    private static String processString(final Object model, final String templateString) throws IOException, TemplateException {
        try (StringWriter result = new StringWriter();
             StringReader reader = new StringReader(templateString)) {
            new Template("string", reader, TEMPLATE_CONFIG).process(model, result);
            return result.toString();
        }
    }
    
    private static void processFile(final Object model, final String templateFile, final String outputFile) throws IOException, TemplateException {
        try (Writer writer = new FileWriter(outputFile)) {
            TEMPLATE_CONFIG.getTemplate(templateFile).process(model, writer);
        }
    }
}
