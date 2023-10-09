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

// package org.apache.shardingsphere.encrypt.metadata.reviser.column;

// import org.apache.shardingsphere.encrypt.rule.EncryptTable;
// import org.apache.shardingsphere.infra.metadata.database.schema.reviser.column.ColumnNameReviser;

// /**
//  * Encrypt column name reviser.
//  */
// public final class EncryptColumnNameReviser implements ColumnNameReviser {

//     private final EncryptTable encryptTable;

//     public EncryptColumnNameReviser(final EncryptTable encryptTable) {
//         this.encryptTable = encryptTable;
//     }

//     @Override
//     public String revise(final String originalName) {
//         if (encryptTable.isCipherColumn(originalName)) {
//             return encryptTable.getLogicColumnByCipherColumn(originalName);
//         }
//         return originalName;
//     }
// }

package org.apache.shardingsphere.encrypt.metadata.reviser.column;

import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.column.ColumnNameReviser;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptColumnNameReviserTest {

    private EncryptColumnNameReviser encryptColumnNameReviser;

    @Before
    public void setUp() {
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.getLogicColumnByPlainColumn("plain_column")).thenReturn("logic_column");
        when(encryptTable.getLogicColumnByCipherColumn("cipher_column")).thenReturn("logic_column");
        when(encryptTable.isCipherColumn("cipher_column")).thenReturn(true);
        when(encryptTable.getPlainColumns()).thenReturn(Collections.singletonList("plain_column"));
        encryptColumnNameReviser = new EncryptColumnNameReviser(encryptTable);
    }

    @Test
    public void assertRevisePlainColumn() {
        assertThat(encryptColumnNameReviser.revise("plain_column"), is("logic_column"));
    }

    @Test
    public void assertReviseCipherColumn() {
        assertThat(encryptColumnNameReviser.revise("cipher_column"), is("logic_column"));
    }

    @Test
    public void assertReviseOtherColumn() {
        assertThat(encryptColumnNameReviser.revise("other_column"), is("other_column"));
    }
}