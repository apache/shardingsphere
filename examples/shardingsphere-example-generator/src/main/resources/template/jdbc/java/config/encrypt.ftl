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
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("phone", "phone", "", "", "phone_plain", "phone_encryptor", null);
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("status", "status", "assisted_query_status", "", "string_encryptor", "string_encryptor", null);
        EncryptTableRuleConfiguration orderItemRule = new EncryptTableRuleConfiguration("t_order_item", Collections.singleton(columnConfigAes), true);
        EncryptTableRuleConfiguration orderRule = new EncryptTableRuleConfiguration("t_order", Collections.singleton(columnConfigTest), true);
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>();
        encryptAlgorithmConfigs.put("phone_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("string_encryptor", new AlgorithmConfiguration("assistedTest", props));
        return new EncryptRuleConfiguration(Arrays.asList(orderRule, orderItemRule), encryptAlgorithmConfigs);
    }
