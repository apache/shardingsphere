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
import lombok.SneakyThrows;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
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
        YamlExampleConfiguration exampleConfiguration = swapConfigToObject();
        Collection<String> products = exampleConfiguration.getProducts();
        for (ExampleGenerator each : ServiceLoader.load(ExampleGenerator.class)) {
            if (products.contains(each.getType())) {
                each.generate(templateConfig, exampleConfiguration);
            }
        }
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlExampleConfiguration swapConfigToObject() {
        URL url = ExampleGeneratorFactory.class.getResource(CONFIG_FILE);
        File file =  null == url ? new File(CONFIG_FILE) : new File(url.toURI().getPath());
        return YamlEngine.unmarshal(file, YamlExampleConfiguration.class);
    }
}
