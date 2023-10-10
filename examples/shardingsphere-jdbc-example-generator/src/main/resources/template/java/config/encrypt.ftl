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
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("phone", new EncryptColumnItemRuleConfiguration("phone", "standard_encryptor"));
        EncryptTableRuleConfiguration orderItemRule = new EncryptTableRuleConfiguration("t_order_item", Collections.singleton(columnConfigAes));
        EncryptColumnRuleConfiguration statusColumnConfig =
            new EncryptColumnRuleConfiguration("status", new EncryptColumnItemRuleConfiguration("status", "standard_encryptor"));
        statusColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("status_assisted", "assisted_encryptor"));
        EncryptTableRuleConfiguration orderRule = new EncryptTableRuleConfiguration("t_order", Collections.singleton(statusColumnConfig)); 
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>();
        encryptAlgorithmConfigs.put("standard_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("assisted_encryptor", new AlgorithmConfiguration("assistedTest", props));
        return new EncryptRuleConfiguration(Arrays.asList(orderRule, orderItemRule), encryptAlgorithmConfigs);
    }
