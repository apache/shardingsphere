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

package org.apache.shardingsphere.orchestration.core.common.rule.converter;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRuleConfigurationConverterTest {
    
    protected static final String SHARDING_RULE_YAML = "tables:\n" + "  t_order:\n" + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "    logicTable: t_order\n" + "    tableStrategy:\n" + "      inline:\n" + "        algorithmExpression: t_order_${order_id % 2}\n"
            + "        shardingColumn: order_id\n";
    
    protected static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    protected static final String ENCRYPT_RULE_YAML = "encryptors:\n" + "  order_encryptor:\n"
            + "    props:\n" + "      aes.key.value: 123456\n" + "    type: aes\n" + "tables:\n" + "  t_order:\n" + "    columns:\n"
            + "      order_id:\n"
            + "        cipherColumn: order_id\n" + "        encryptor: order_encryptor\n";
    
    protected static final String SHADOW_RULE_YAML = "column: shadow\n"
            + "shadowMappings:\n"
            + "  ds: shadow_ds\n";
    
    protected static final String SHARDING_NAME = "sharding_db";
}

