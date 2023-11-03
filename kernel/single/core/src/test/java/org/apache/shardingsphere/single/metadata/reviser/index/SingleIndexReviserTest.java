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

package org.apache.shardingsphere.single.metadata.reviser.index;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleIndexReviserTest {
    
    @Test
    void assertReviserReturnsRevisedIndex() {
        IndexMetaData originalMetaData = new IndexMetaData("test_idx_tableName");
        originalMetaData.getColumns().add("column1");
        originalMetaData.getColumns().add("column2");
        originalMetaData.setUnique(true);
        SingleIndexReviser reviser = new SingleIndexReviser();
        Optional<IndexMetaData> optionalRevised = reviser.revise("tableName", originalMetaData, null);
        assertTrue(optionalRevised.isPresent());
        IndexMetaData actual = optionalRevised.get();
        assertThat(originalMetaData.isUnique(), is(actual.isUnique()));
        assertThat(originalMetaData.getColumns(), is(actual.getColumns()));
        assertThat("test_idx", is(actual.getName()));
    }
}
