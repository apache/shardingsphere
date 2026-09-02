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

package org.apache.shardingsphere.encrypt.rule.column.item;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CipherColumnItemTest {
    
    @Test
    void assertEncryptNullValue() {
        assertNull(new CipherColumnItem("foo_col", mockEncryptAlgorithm()).encrypt("foo-db", "foo_schema", "foo_tbl", "foo_col", (Object) null));
    }
    
    @Test
    void assertEncryptSingleValue() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm();
        when(encryptAlgorithm.encrypt(eq("foo_value"), deepEq(new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col"))))
                .thenReturn("encrypted_foo_value");
        CipherColumnItem cipherColumnItem = new CipherColumnItem("foo_col", encryptAlgorithm);
        assertThat(cipherColumnItem.encrypt("foo_db", "foo_schema", "foo_tbl", "foo_col", "foo_value"), is("encrypted_foo_value"));
    }
    
    @Test
    void assertEncryptMultipleValues() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm();
        when(encryptAlgorithm.encrypt(eq("foo_value"), deepEq(new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col"))))
                .thenReturn("encrypted_foo_value");
        CipherColumnItem cipherColumnItem = new CipherColumnItem("foo_col", encryptAlgorithm);
        assertThat(cipherColumnItem.encrypt("foo_db", "foo_schema", "foo_tbl", "foo_col", Arrays.asList(null, "foo_value")), is(Arrays.asList(null, "encrypted_foo_value")));
    }
    
    @Test
    void assertDecryptNullValue() {
        assertNull(new CipherColumnItem("foo_col", mockEncryptAlgorithm()).decrypt("foo-db", "foo_schema", "foo_tbl", "foo_col", null));
    }
    
    @Test
    void assertDecrypt() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm();
        when(encryptAlgorithm.decrypt(eq("encrypted_foo_value"), deepEq(new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col"))))
                .thenReturn("foo_value");
        CipherColumnItem cipherColumnItem = new CipherColumnItem("foo_col", encryptAlgorithm);
        assertThat(cipherColumnItem.decrypt("foo_db", "foo_schema", "foo_tbl", "foo_col", "encrypted_foo_value"), is("foo_value"));
    }
    
    private EncryptAlgorithm mockEncryptAlgorithm() {
        EncryptAlgorithm result = mock(EncryptAlgorithm.class);
        when(result.getMetaData()).thenReturn(new EncryptAlgorithmMetaData(true, true, false, String.class));
        when(result.getEncoder()).thenReturn(Optional.empty());
        when(result.toConfiguration()).thenReturn(new AlgorithmConfiguration("FIXTURE", new Properties()));
        return result;
    }
}
