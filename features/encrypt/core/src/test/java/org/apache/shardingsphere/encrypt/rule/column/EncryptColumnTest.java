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

package org.apache.shardingsphere.encrypt.rule.column;

import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class EncryptColumnTest {
    
    @Test
    void assertGetQueryEncryptorWithoutAssistedQuery() {
        EncryptAlgorithm cipherAlgorithm = mock(EncryptAlgorithm.class);
        assertThat(new EncryptColumn("foo_tbl", new CipherColumnItem("foo_col", cipherAlgorithm)).getQueryEncryptor(), is(cipherAlgorithm));
    }
    
    @Test
    void assertGetQueryEncryptorWithAssistedQuery() {
        EncryptColumn encryptColumn = new EncryptColumn("foo_tbl", new CipherColumnItem("foo_cipher_col", mock(EncryptAlgorithm.class)));
        EncryptAlgorithm assistedQueryAlgorithm = mock(EncryptAlgorithm.class);
        encryptColumn.setAssistedQuery(new AssistedQueryColumnItem("foo_assisted_query_col", assistedQueryAlgorithm));
        assertThat(encryptColumn.getQueryEncryptor(), is(assistedQueryAlgorithm));
    }
}
