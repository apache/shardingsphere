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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UniqueKeyInventoryDataRecordPositionCreatorTest {
    
    @Test
    void assertCreate() throws SQLException {
        assertThat(new UniqueKeyInventoryDataRecordPositionCreator().create(mockInventoryDumperContext(), mock(ResultSet.class)), isA(UnsupportedKeyIngestPosition.class));
    }
    
    @SuppressWarnings("unchecked")
    private InventoryDumperContext mockInventoryDumperContext() {
        InventoryDumperContext result = mock(InventoryDumperContext.class, RETURNS_DEEP_STUBS);
        PrimaryKeyIngestPosition<Object> ingestPosition = mock(PrimaryKeyIngestPosition.class);
        when(ingestPosition.getEndValue()).thenReturn(new Object());
        when(result.getCommonContext().getPosition()).thenReturn(ingestPosition);
        return result;
    }
}
