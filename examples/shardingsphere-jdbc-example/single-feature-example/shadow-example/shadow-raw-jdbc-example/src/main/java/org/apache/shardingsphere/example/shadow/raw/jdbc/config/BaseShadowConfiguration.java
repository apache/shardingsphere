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

package org.apache.shardingsphere.example.shadow.raw.jdbc.config;

import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public abstract class BaseShadowConfiguration implements ExampleConfiguration {
    
    protected Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("ds", DataSourceUtil.createDataSource("demo_ds"));
        result.put("ds_shadow", DataSourceUtil.createDataSource("shadow_demo_ds"));
        return result;
    }
    
    protected Properties createShardingSphereProps() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        return result;
    }
    
    protected SQLParserRuleConfiguration createSQLParserRuleConfiguration() {
        CacheOption parseTreeCacheOption = new CacheOption(128, 1024L, false);
        CacheOption sqlStatementCacheOption = new CacheOption(2000, 65535L, false);
        return new SQLParserRuleConfiguration(true, parseTreeCacheOption, sqlStatementCacheOption);
    }
    
    protected Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user-id-insert-match-algorithm");
        result.add("user-id-delete-match-algorithm");
        result.add("user-id-select-match-algorithm");
        result.add("simple-hint-algorithm");
        return result;
    }
    
    protected Map<String, AlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_type");
        userIdInsertProps.setProperty("value", "1");
        result.put("user-id-insert-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
        Properties userIdDeleteProps = new Properties();
        userIdDeleteProps.setProperty("operation", "delete");
        userIdDeleteProps.setProperty("column", "user_type");
        userIdDeleteProps.setProperty("value", "1");
        result.put("user-id-delete-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdDeleteProps));
        Properties userIdSelectProps = new Properties();
        userIdSelectProps.setProperty("operation", "select");
        userIdSelectProps.setProperty("column", "user_type");
        userIdSelectProps.setProperty("value", "1");
        result.put("user-id-select-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdSelectProps));
        Properties noteAlgorithmProps = new Properties();
        noteAlgorithmProps.setProperty("shadow", Boolean.TRUE.toString());
        noteAlgorithmProps.setProperty("foo", "bar");
        result.put("simple-hint-algorithm", new AlgorithmConfiguration("SIMPLE_HINT", noteAlgorithmProps));
        return result;
    }
}
