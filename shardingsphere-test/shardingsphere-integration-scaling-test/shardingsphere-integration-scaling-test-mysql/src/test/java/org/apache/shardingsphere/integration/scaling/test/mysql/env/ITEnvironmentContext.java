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

package org.apache.shardingsphere.integration.scaling.test.mysql.env;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.cases.DataSet;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.cases.Type;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.config.SourceConfiguration;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.config.TargetConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test environment context.
 */
@Getter
public final class ITEnvironmentContext {
    
    public static final ITEnvironmentContext INSTANCE = new ITEnvironmentContext();
    
    private static final String TYPE_TEST_XML = "/cases/mysql/types.xml";
    
    private final DataSet testCases;
    
    private final DataSource sourceDataSource;
    
    private final DataSource targetDataSource;
    
    private final String scalingConfiguration;
    
    public ITEnvironmentContext() {
        testCases = loadTestCases();
        Map<String, YamlTableRuleConfiguration> sourceTableRules = createSourceTableRules(testCases);
        scalingConfiguration = createScalingConfiguration(sourceTableRules);
        sourceDataSource = SourceConfiguration.createHostDataSource(sourceTableRules);
        targetDataSource = TargetConfiguration.createHostDataSource();
    }
    
    @SneakyThrows
    private DataSet loadTestCases() {
        try (FileReader reader = new FileReader(ITEnvironmentContext.class.getResource(TYPE_TEST_XML).getPath())) {
            return (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private Map<String, YamlTableRuleConfiguration> createSourceTableRules(final DataSet dataSet) {
        Map<String, YamlTableRuleConfiguration> result = new HashMap<>();
        for (Type type : testCases.getTypes()) {
            YamlTableRuleConfiguration tableRule = new YamlTableRuleConfiguration();
            tableRule.setLogicTable(type.getTableName());
            tableRule.setActualDataNodes("ds_src." + type.getTableName());
            result.put(type.getTableName(), tableRule);
        }
        return result;
    }
    
    private static String createScalingConfiguration(final Map<String, YamlTableRuleConfiguration> tableRules) {
        JobConfiguration jobConfiguration = new JobConfiguration();
        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setSource(SourceConfiguration.getDockerConfiguration(tableRules).wrap());
        ruleConfiguration.setTarget(TargetConfiguration.getDockerConfiguration().wrap());
        jobConfiguration.setRuleConfig(ruleConfiguration);
        return new Gson().toJson(jobConfiguration);
    }
}
