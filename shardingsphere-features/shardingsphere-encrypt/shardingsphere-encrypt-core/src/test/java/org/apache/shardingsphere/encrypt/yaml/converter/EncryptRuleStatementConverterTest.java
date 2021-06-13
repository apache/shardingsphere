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

package org.apache.shardingsphere.encrypt.yaml.converter;

import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptColumnSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public final class EncryptRuleStatementConverterTest {

    @Test
    public void assertCovert() {
        YamlEncryptRuleConfiguration encryptRuleConfiguration = EncryptRuleStatementConverter.convert(Collections
                .singleton(new EncryptRuleSegment("t_encrypt", buildColumns())));
        assertNotNull(encryptRuleConfiguration);
        assertThat(encryptRuleConfiguration.getTables().keySet(), is(Collections.singleton("t_encrypt")));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getName(), is("t_encrypt"));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getColumns().keySet(), is(Collections.singleton("user_id")));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getColumns().get("user_id").getLogicColumn(), is("user_id"));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getColumns().get("user_id").getCipherColumn(), is("user_cipher"));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getColumns().get("user_id").getPlainColumn(), is("user_plain"));
        assertThat(encryptRuleConfiguration.getTables().get("t_encrypt").getColumns().get("user_id").getEncryptorName(), is("t_encrypt_user_id"));
    }

    private Collection<EncryptColumnSegment> buildColumns() {
        EncryptColumnSegment encryptColumnSegment = new EncryptColumnSegment();
        encryptColumnSegment.setName("user_id");
        encryptColumnSegment.setPlainColumn("user_plain");
        encryptColumnSegment.setCipherColumn("user_cipher");
        FunctionSegment functionSegment = new FunctionSegment();
        functionSegment.setAlgorithmName("MD5");
        Properties properties = new Properties();
        properties.setProperty("MD5-key", "MD5-value");
        functionSegment.setAlgorithmProps(properties);
        encryptColumnSegment.setEncryptor(functionSegment);
        return Collections.singleton(encryptColumnSegment);
    }
}
