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
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Example generator factory.
 */
public final class ExampleGeneratorFactory {
    
    private static final String CONFIG_FILE = "/config.yaml";
    
    private final Configuration templateConfig;
    
    public ExampleGeneratorFactory() throws IOException {
        templateConfig = createTemplateConfiguration();
    }
    
    private Configuration createTemplateConfiguration() throws IOException {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(ExampleGeneratorFactory.class.getClassLoader().getResource("template")).getFile()));
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Generate directories and files by template.
     * 
     * @throws TemplateException template exception
     * @throws IOException IO exception
     */
    @SuppressWarnings("unchecked")
    public void generate() throws TemplateException, IOException {
        try (InputStream input = ExampleGeneratorFactory.class.getResourceAsStream(CONFIG_FILE)) {
            Map<String, String> dataModel = new Yaml().loadAs(input, Map.class);
            String product = dataModel.get("product");
            dataModel.put("shardingsphereVersion", ShardingSphereVersion.VERSION);
            for (ExampleGenerator each : ServiceLoader.load(ExampleGenerator.class)) {
                if (product.equals(each.getType())) {
                    each.generate(templateConfig, dataModel);
                }
            }
        }
    }
}
