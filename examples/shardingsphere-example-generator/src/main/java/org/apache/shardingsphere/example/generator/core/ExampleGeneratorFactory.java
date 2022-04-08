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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

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
        checkYamlExampleConfiguration(exampleConfiguration);
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
    
    private void checkYamlExampleConfiguration(final YamlExampleConfiguration configuration) {
        List<String> products = Optional.ofNullable(configuration.getProducts()).orElse(Collections.emptyList());
        Set<String> productsSets = Sets.newHashSet("jdbc", "proxy");
        products.stream().forEach(v -> {
            Preconditions.checkArgument(productsSets.contains(v), getErrorMessage("products", productsSets, v));
        });
        List<String> modes = Optional.ofNullable(configuration.getModes()).orElse(Collections.emptyList());
        Set<String> modesSets = Sets.newHashSet("memory", "proxy", "cluster-zookeeper", "cluster-etcd", "standalone-file");
        modes.stream().forEach(v -> {
            Preconditions.checkArgument(modesSets.contains(v), getErrorMessage("modes", modesSets, v));
        });
        List<String> transactions = Optional.ofNullable(configuration.getTransactions()).orElse(Collections.emptyList());
        Set<String> transactionsSets = Sets.newHashSet("local");
        transactions.stream().forEach(v -> {
            Preconditions.checkArgument(transactionsSets.contains(v), getErrorMessage("transactions", transactionsSets, v));
        });
        List<String> features = Optional.ofNullable(configuration.getFeatures()).orElse(Collections.emptyList());
        Set<String> featuresSets = Sets.newHashSet("sharding", "readwrite-splitting", "encrypt", "db-discovery");
        features.stream().forEach(v -> {
            Preconditions.checkArgument(featuresSets.contains(v), getErrorMessage("features", featuresSets, v));
        });
        List<String> frameworks = Optional.ofNullable(configuration.getFrameworks()).orElse(Collections.emptyList());
        Set<String> frameworksSets = Sets.newHashSet("jdbc", "spring-boot-starter-jdbc", "spring-boot-starter-jpa", "spring-boot-starter-mybatis", "spring-namespace-jdbc", "spring-namespace-jpa", "spring-namespace-mybatis");
        frameworks.stream().forEach(v -> {
            Preconditions.checkArgument(frameworksSets.contains(v), getErrorMessage("frameworks", frameworksSets, v));
        });
        Set<String> propsSets = Sets.newHashSet("host", "port", "username", "password");
        configuration.getProps().forEach((key, value) -> {
            Preconditions.checkArgument(propsSets.contains(key) && value != null, "Example configuration(in the config.yaml) error: " + key + " in props must not be empty");
        });
    }
    
    private String getErrorMessage(final String configItem, final Set<String> correctValues, final String errorValue) {
        return "Example configuration(in the config.yaml) error in the \"" + configItem + "\"" + ",it only supports:" + correctValues.toString() + ",the currently configured value:" + errorValue;
    }
}
