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
        result.setDefaultShadowAlgorithmName("sql-hint-algorithm");
        result.setDataSources(createShadowDataSources());
        result.setTables(createShadowTables());
        return result;
    } 
            
    private RuleConfiguration createSQLParserRuleConfiguration() {
        return new SQLParserRuleConfiguration(new CacheOption(128, 1024L), new CacheOption(2000, 65535L));
    }
    
    private Map<String, ShadowTableConfiguration> createShadowTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_order", new ShadowTableConfiguration(createDataSourceNames(), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("order-type-insert-match-algorithm");
        result.add("order-type-delete-match-algorithm");
        result.add("order-type-select-match-algorithm");
        result.add("sql-hint-algorithm");
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        result.add("shadow-data-source");
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> createShadowDataSources() {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        result.add(new ShadowDataSourceConfiguration("shadow-data-source", "ds_0", "ds_1"));
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties orderTypeInsertProps = new Properties();
        orderTypeInsertProps.setProperty("operation", "insert");
        orderTypeInsertProps.setProperty("column", "order_type");
        orderTypeInsertProps.setProperty("value", "1");
        result.put("order-type-insert-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", orderTypeInsertProps));
        Properties orderTypeDeleteProps = new Properties();
        orderTypeDeleteProps.setProperty("operation", "delete");
        orderTypeDeleteProps.setProperty("column", "order_type");
        orderTypeDeleteProps.setProperty("value", "1");
        result.put("order-type-delete-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", orderTypeDeleteProps));
        Properties orderTypeSelectProps = new Properties();
        orderTypeSelectProps.setProperty("operation", "select");
        orderTypeSelectProps.setProperty("column", "order_type");
        orderTypeSelectProps.setProperty("value", "1");
        result.put("order-type-select-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", orderTypeSelectProps));
        Properties noteAlgorithmProps = new Properties();
        noteAlgorithmProps.setProperty("shadow", "true");
        noteAlgorithmProps.setProperty("foo", "bar");
        result.put("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", noteAlgorithmProps));
        return result;
    }
