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

package org.apache.sharding.example.engine;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Example generate engine.
 */
public final class ExampleGenerateEngine {
    
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_31);
    
    private static final String OUTPUT_PATH = "./examples/shardingsphere-sample/shardingsphere-jdbc-sample/shardingsphere-jdbc-${mode}-example"
            + "/shardingsphere-jdbc-${mode}-${transaction}-example/shardingsphere-jdbc-${mode}-${transaction}-${feature}-example"
            + "/shardingsphere-jdbc-${mode}-${transaction}-${feature}-${framework}-example/src/main/java/org/apache/shardingsphere/example/${feature}/${framework}";

    private static final String FILE_NAME_PREFIX = "${mode?cap_first}${transaction?cap_first}${feature?cap_first}${framework?cap_first}";
    
    private static final Map<String, String> TEMPLATE_MAP = new HashMap(4, 1);
    static {
        try {
            CONFIGURATION.setDirectoryForTemplateLoading(new File(ExampleGenerateEngine.class.getClassLoader().getResource("").getFile()));
            CONFIGURATION.setDefaultEncoding("UTF-8");
            TEMPLATE_MAP.put("Configuration", "Configuration.ftl");
            TEMPLATE_MAP.put("Example", "Example.ftl");
            TEMPLATE_MAP.put("ExampleService", "ExampleService.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generate files based on data model.
     * @param model data model
     * @param templateFile Equivalent to the template name of the template base directory.
     * @param outputFile Output directory and file name.
     */
    public static void processFile(final Object model, final String templateFile, final String outputFile) {
        try {
            Template template = CONFIGURATION.getTemplate(templateFile);
            template.process(model, new FileWriter(outputFile));
        } catch (IOException | TemplateException e) {
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
        try {
            StringWriter result = new StringWriter();
            Template t = new Template("string", new StringReader(templateString), CONFIGURATION);
            t.process(model, result);
            return result.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream in = ExampleGenerateEngine.class.getResourceAsStream("/datamodel.yaml");
        Map<String, String> map = yaml.loadAs(in, Map.class);
        String fileName = processString(map, FILE_NAME_PREFIX);
        String outputPath = processString(map, OUTPUT_PATH);
        for (String key : TEMPLATE_MAP.keySet()) {
            processFile(map, "/template/jdbc/" + TEMPLATE_MAP.get(key), outputPath + "/" + fileName + key + ".java");
        }
    }
}

