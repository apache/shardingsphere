<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
    
    private RuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(createShadowAlgorithmConfigurations());
        result.setDataSources(createShadowDataSources());
        result.setTables(createShadowTables());
        return result;
    } 
            
    private RuleConfiguration createSQLParserRuleConfiguration() {
        SQLParserRuleConfiguration result = new DefaultSQLParserRuleConfigurationBuilder().build(); 
        result.setSqlCommentParseEnabled(true);
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createShadowTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_order", new ShadowTableConfiguration(createDataSourceNames(), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user-id-insert-match-algorithm");
        result.add("user-id-delete-match-algorithm");
        result.add("user-id-select-match-algorithm");
        result.add("simple-hint-algorithm");
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        result.add("shadow-data-source");
        return result;
    }

    private Map<String, ShadowDataSourceConfiguration> createShadowDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds_0", "ds_1"));
        return result;
    }

    private Map<String, AlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "order_type");
        userIdInsertProps.setProperty("value", "1");
        result.put("user-id-insert-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
        Properties userIdDeleteProps = new Properties();
        userIdDeleteProps.setProperty("operation", "delete");
        userIdDeleteProps.setProperty("column", "order_type");
        userIdDeleteProps.setProperty("value", "1");
        result.put("user-id-delete-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdDeleteProps));
        Properties userIdSelectProps = new Properties();
        userIdSelectProps.setProperty("operation", "select");
        userIdSelectProps.setProperty("column", "order_type");
        userIdSelectProps.setProperty("value", "1");
        result.put("user-id-select-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdSelectProps));
        Properties noteAlgorithmProps = new Properties();
        noteAlgorithmProps.setProperty("shadow", "true");
        noteAlgorithmProps.setProperty("foo", "bar");
        result.put("simple-hint-algorithm", new AlgorithmConfiguration("SIMPLE_HINT", noteAlgorithmProps));
        return result;
    }
