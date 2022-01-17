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

package org.apache.shardingsphere.example.engine;

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
    
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_31);
    
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
    
    private static final String FILE_NAME_PREFIX = "${mode?cap_first}${transaction?cap_first}"
            + "<#assign featureName=\"\">"
            + "<#if feature?split(\",\")?size gt 1>"
            + "<#assign featureName=\"Mixed\">"
            + "<#else>"
            + "<#list feature?split(\"-\") as item>"
            + "<#assign featureName=featureName + item?cap_first>"
            + "</#list></#if>${featureName}"
            + "<#assign frameworkName=\"\">"
            + "<#list framework?split(\"-\") as item>"
            + "<#assign frameworkName=frameworkName + item?cap_first>"
            + "</#list>${frameworkName}";
    
    private static final String DATA_MODEL_PATH = "/data-model/data-model.yaml";
    
    private static Map<String, String> renameTemplateMap;
    
    private static Map<String, String> unRenameTemplateMap;
    
    private static Map<String, String> resourceTemplateMap;
    
    static {
        try {
            CONFIGURATION.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(ExampleGenerateEngine.class.getClassLoader().getResource("")).getFile()));
            CONFIGURATION.setDefaultEncoding("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generate file 
     * @param args args
     */
    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream in = ExampleGenerateEngine.class.getResourceAsStream(DATA_MODEL_PATH);
        Map<String, String> dataModel = yaml.loadAs(in, Map.class);
        fillTemplateMap(dataModel);
        generateJavaCode(dataModel);
        generateResourcesFile(dataModel);
    }
    
    /**
     * Generate files based on data model.
     * @param model data model
     * @param templateFile Equivalent to the template name of the template base directory.
     * @param outputFile Output directory and file name.
     */
    public static void processFile(final Object model, final String templateFile, final String outputFile) {
        try (Writer writer = new FileWriter(outputFile)) {
            Template template = CONFIGURATION.getTemplate(templateFile);
            template.process(model, writer);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Placeholder replacement.
     * @param model data model
     * @param templateString String template
     * @return Replace the placeholder string
     */
    public static String processString(final Object model, final String templateString) {
        try (StringWriter result = new StringWriter();
             StringReader reader = new StringReader(templateString)) {
            Template t = new Template("string", reader, CONFIGURATION);
            t.process(model, result);
            return result.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void fillTemplateMap(Map<String, String> dataModel) {
        renameTemplateMap = ExampleTemplateFactory.getRenameTemplate(dataModel);
        unRenameTemplateMap = ExampleTemplateFactory.getUnReNameTemplate(dataModel);
        resourceTemplateMap = ExampleTemplateFactory.getResourceTemplate(dataModel);
    }
    
    private static void generateJavaCode(final Map<String, String> dataModel) {
        String fileName = processString(dataModel, FILE_NAME_PREFIX);
        String outputPath = processString(dataModel, OUTPUT_PATH + JAVA_CLASS_PATH);
        for (String key : renameTemplateMap.keySet()) {
            processFile(dataModel, "/template/" + renameTemplateMap.get(key), outputPath + "/" + fileName + key + ".java");
        }
        for (String key : unRenameTemplateMap.keySet()) {
            processFile(dataModel, "/template/" + key + ".ftl", outputPath + "/" + unRenameTemplateMap.get(key));
        }
    }
    
    private static void generateResourcesFile(final Map<String, String> dataModel) {
        String outputPath = processString(dataModel, OUTPUT_PATH + RESOURCES_PATH);
        for (String key : resourceTemplateMap.keySet()) {
            processFile(dataModel, "/template/" + key + ".ftl", outputPath + "/" + resourceTemplateMap.get(key));
        }
    }
}

