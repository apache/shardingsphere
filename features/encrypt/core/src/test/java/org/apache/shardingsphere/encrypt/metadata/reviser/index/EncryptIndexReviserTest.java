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

package org.apache.shardingsphere.encrypt.metadata.reviser.index;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptIndexReviserTest {
    
    @Test
    void assertReviseWithEmptyColumn() {
        assertFalse(new EncryptIndexReviser(mock(EncryptTable.class)).revise("foo_tbl", new IndexMetaData("foo_idx"), mock(EncryptRule.class)).isPresent());
    }
    
    @Test
    void assertReviseWithColumns() {
        Optional<IndexMetaData> actual = new EncryptIndexReviser(mockEncryptTable())
                .revise("foo_tbl", new IndexMetaData("foo_idx", Arrays.asList("cipher_col", "assisted_col", "other_col")), mock(EncryptRule.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("foo_idx"));
        assertThat(actual.get().getColumns(), is(new LinkedHashSet<>(Arrays.asList("col_1", "col_2", "other_col"))));
    }
    
    private EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class);
        when(result.isCipherColumn("cipher_col")).thenReturn(true);
        when(result.getLogicColumnByCipherColumn("cipher_col")).thenReturn("col_1");
        when(result.isAssistedQueryColumn("assisted_col")).thenReturn(true);
        when(result.getLogicColumnByAssistedQueryColumn("assisted_col")).thenReturn("col_2");
        return result;
    }
}
