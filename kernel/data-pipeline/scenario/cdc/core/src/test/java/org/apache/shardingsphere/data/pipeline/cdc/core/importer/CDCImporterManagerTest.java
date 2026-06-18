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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCImporterManagerTest {
    
    private CDCImporter actual;
    
    @BeforeEach
    void setUp() {
        actual = mock(CDCImporter.class);
        when(actual.getImporterId()).thenReturn("nish8gx8");
    }
    
    @Test
    void assertPutImporter() {
        CDCImporterManager.putImporter(actual);
        CDCImporter expected = CDCImporterManager.getImporter(actual.getImporterId());
        assertThat(actual.getImporterId(), is(expected.getImporterId()));
    }
    
    @Test
    void assertGetImporter() {
        CDCImporterManager.putImporter(actual);
        CDCImporter expected = CDCImporterManager.getImporter(actual.getImporterId());
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertRemoveImporter() {
        CDCImporterManager.putImporter(actual);
        CDCImporterManager.removeImporter(actual.getImporterId());
        assertNull(CDCImporterManager.getImporter(actual.getImporterId()));
    }
    
}
