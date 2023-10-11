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

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleStatementConverterTest {
    
    @Test
    void assertCovert() {
        EncryptRuleConfiguration actual = EncryptRuleStatementConverter.convert(Collections.singleton(new EncryptRuleSegment("t_encrypt", createColumns())));
        assertThat(actual.getTables().iterator().next().getName(), is("t_encrypt"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getName(), is("user_id"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getCipher().getName(), is("user_cipher"));
        assertTrue(actual.getTables().iterator().next().getColumns().iterator().next().getAssistedQuery().isPresent());
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getAssistedQuery().get().getName(), is("assisted_column"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getCipher().getEncryptorName(), is("t_encrypt_user_id"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getAssistedQuery().get().getEncryptorName(), is("assist_t_encrypt_user_id"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getLikeQuery().get().getEncryptorName(), is("like_t_encrypt_user_id"));
    }
    
    private Collection<EncryptColumnSegment> createColumns() {
        return Collections.singleton(new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", PropertiesBuilder.build(new Property("MD5-key", "MD5-value")))),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("MD5", PropertiesBuilder.build(new Property("MD5-key", "MD5-value")))),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("MD5", PropertiesBuilder.build(new Property("MD5-key", "MD5-value"))))));
    }
}
