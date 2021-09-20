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
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Example generate engine.
 */
public final class ExampleGenerateEngine {
    
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_31);
    
    private static final String DEFAULT_BASE_TEMPLATE_PATH = ExampleGenerateEngine.class.getClassLoader().getResource("templates").getPath();
    
    public ExampleGenerateEngine() throws IOException {
        this(DEFAULT_BASE_TEMPLATE_PATH);
    }
    
    public ExampleGenerateEngine(final String path) throws IOException {
        CONFIGURATION.setDirectoryForTemplateLoading(new File(path));
    }

    /**
     * Generate files based on data model.
     * @param obj data model
     * @param templateFile Equivalent to the template name of the template base directory.
     * @param outputFile Output directory and file name
     */
    public void process(final Object obj, final String templateFile, final String outputFile) {
        try {
            Template template = CONFIGURATION.getTemplate(templateFile);
            template.process(obj, new FileWriter(outputFile));
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
    
    private static YamlEncryptRuleConfiguration buildEncryptRule() {
        YamlEncryptRuleConfiguration encryptRuleConfiguration = new YamlEncryptRuleConfiguration();
        YamlEncryptTableRuleConfiguration tableRuleConfiguration = new YamlEncryptTableRuleConfiguration();
        YamlEncryptColumnRuleConfiguration encryptColumnRuleConfiguration = new YamlEncryptColumnRuleConfiguration();
        encryptColumnRuleConfiguration.setCipherColumn("cipher");
        encryptColumnRuleConfiguration.setEncryptorName("EncryptorName");
        encryptColumnRuleConfiguration.setLogicColumn("logic");
        encryptColumnRuleConfiguration.setPlainColumn("plain");
        encryptColumnRuleConfiguration.setAssistedQueryColumn("AssistedQueryColumn");
        tableRuleConfiguration.setName("table");
        Map<String, YamlEncryptColumnRuleConfiguration> encryptColumnMap = new HashMap<>();
        encryptColumnMap.put("encryptColumn", encryptColumnRuleConfiguration);
        tableRuleConfiguration.setColumns(encryptColumnMap);
        Map<String, YamlEncryptTableRuleConfiguration> encryptMap = new HashMap<>();
        encryptMap.put("table", tableRuleConfiguration);
        YamlShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration = new YamlShardingSphereAlgorithmConfiguration();
        shardingSphereAlgorithmConfiguration.setType("test");
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        shardingSphereAlgorithmConfiguration.setProps(properties);
        Map<String, YamlShardingSphereAlgorithmConfiguration> encryptors = new LinkedHashMap<>();
        encryptors.put("encryptor", shardingSphereAlgorithmConfiguration);
        encryptRuleConfiguration.setTables(encryptMap);
        encryptRuleConfiguration.setEncryptors(encryptors);
        return encryptRuleConfiguration;
    }
    
    public static void main(String[] args) throws IOException {
        ExampleGenerateEngine engine = new ExampleGenerateEngine();
        Map<String, Object> map = new HashMap<>();
        map.put("encrypt", buildEncryptRule());
        engine.process(map, "encryptRuleYamlTemplate.ftl", DEFAULT_BASE_TEMPLATE_PATH + "/test.yaml");
    }
}
