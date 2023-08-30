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

import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfiguration;
import org.apache.shardingsphere.example.generator.core.yaml.config.YamlExampleConfigurationValidator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

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
        result.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("template")).getFile()));
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Generate directories and files by template.
     *
     * @throws TemplateException template exception
     * @throws IOException IO exception
     */
    public void generate() throws TemplateException, IOException {
        YamlExampleConfiguration exampleConfig = buildExampleConfiguration();
        YamlExampleConfigurationValidator.validate(exampleConfig);
        for (String each : exampleConfig.getProducts()) {
            TypedSPILoader.getService(ExampleGenerator.class, each).generate(templateConfig, exampleConfig);
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
        result.setProducts(Collections.singletonList("jdbc"));
        return result;
    }
    
    private List<String> getSysEnvByKey(final Properties props, final String key) {
        return Lists.newArrayList(props.getProperty(key).split(","));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlExampleConfiguration swapConfigToObject() {
        URL url = ExampleGeneratorFactory.class.getResource(CONFIG_FILE);
        File file = null == url ? new File(CONFIG_FILE) : new File(url.toURI().getPath());
        return YamlEngine.unmarshal(file, YamlExampleConfiguration.class);
    }
}
