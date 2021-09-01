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

package org.apache.shardingsphere.encrypt.distsql.handler.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.junit.Test;
import org.mockito.Mock;

public final class EncryptRuleStatementConverterTest {
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Test
    public void assertCovert() {
        EncryptRuleConfiguration encryptRuleConfiguration = EncryptRuleStatementConverter.convert(metaData, Collections.singleton(new EncryptRuleSegment("t_encrypt", buildColumns())));
        assertNotNull(encryptRuleConfiguration);
        assertThat(encryptRuleConfiguration.getTables().iterator().next().getName(), is("t_encrypt"));
        assertThat(encryptRuleConfiguration.getTables().iterator().next().getColumns().iterator().next().getLogicColumn(), is("user_id"));
        assertThat(encryptRuleConfiguration.getTables().iterator().next().getColumns().iterator().next().getCipherColumn(), is("user_cipher"));
        assertThat(encryptRuleConfiguration.getTables().iterator().next().getColumns().iterator().next().getPlainColumn(), is("user_plain"));
        assertThat(encryptRuleConfiguration.getTables().iterator().next().getColumns().iterator().next().getEncryptorName(), is("t_encrypt_user_id"));
    }
    
    private Collection<EncryptColumnSegment> buildColumns() {
        Properties props = new Properties();
        props.setProperty("MD5-key", "MD5-value");
        return Collections.singleton(new EncryptColumnSegment("user_id", "user_cipher", "user_plain", new AlgorithmSegment("MD5", props)));
    }
}
