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

package org.apache.shardingsphere.encrypt.metadata.converter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptNodeConverterTest {
    
    @Test
    void assertGetTableNamePath() {
        assertThat(EncryptNodeConverter.getTableNamePath("foo_table"), is("tables/foo_table"));
    }
    
    @Test
    void assertGetAlgorithmPath() {
        assertThat(EncryptNodeConverter.getEncryptorPath("AES"), is("encryptors/AES"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(EncryptNodeConverter.isEncryptPath("/metadata/foo_db/rules/encrypt/tables/foo_table"));
        assertFalse(EncryptNodeConverter.isEncryptPath("/metadata/foo_db/rules/foo/tables/foo_table"));
        assertTrue(EncryptNodeConverter.isTablePath("/metadata/foo_db/rules/encrypt/tables/foo_table"));
        assertFalse(EncryptNodeConverter.isTablePath("/metadata/foo_db/rules/encrypt/encryptors/AES"));
        assertTrue(EncryptNodeConverter.isEncryptorPath("/metadata/foo_db/rules/encrypt/encryptors/AES"));
        assertFalse(EncryptNodeConverter.isEncryptorPath("/metadata/foo_db/rules/encrypt/tables/foo_table"));
    }
    
    @Test
    void assertGetTableNameByRulePath() {
        Optional<String> actual = EncryptNodeConverter.getTableName("/metadata/foo_db/rules/encrypt/tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetAlgorithmNameByRulePath() {
        Optional<String> actual = EncryptNodeConverter.getEncryptorName("/metadata/foo_db/rules/encrypt/encryptors/AES");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("AES"));
    }
    
    @Test
    void assertGetEncryptTableVersion() {
        Optional<String> actual = EncryptNodeConverter.getEncryptTableVersion("/metadata/foo_db/rules/encrypt/tables/foo_table/versions/1");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("1"));
    }
    
    @Test
    void assertGetEncryptAlgorithmVersion() {
        Optional<String> actual = EncryptNodeConverter.getEncryptorVersion("/metadata/foo_db/rules/encrypt/encryptors/aes_algorithm/versions/1");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("1"));
    }
}
