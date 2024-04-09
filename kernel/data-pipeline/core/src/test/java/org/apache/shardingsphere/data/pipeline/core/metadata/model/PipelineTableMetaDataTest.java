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

package org.apache.shardingsphere.data.pipeline.core.metadata.model;

import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineTableMetaDataTest {
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    @BeforeEach
    void setUp() {
        PipelineColumnMetaData column = new PipelineColumnMetaData(1, "test", Types.INTEGER, "INTEGER", true, true, true);
        pipelineTableMetaData = new PipelineTableMetaData("test_data", Collections.singletonMap(new CaseInsensitiveIdentifier("test"), column), Collections.emptySet());
    }
    
    @Test
    void assertGetColumnMetaDataGivenColumnIndex() {
        PipelineColumnMetaData actual = pipelineTableMetaData.getColumnMetaData(1);
        assertThat(actual.getOrdinalPosition(), is(1));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    void assertGetColumnMetaDataGivenColumnName() {
        PipelineColumnMetaData actual = pipelineTableMetaData.getColumnMetaData("test");
        assertNull(pipelineTableMetaData.getColumnMetaData("non_exist"));
        assertThat(actual.getOrdinalPosition(), is(1));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    void assertIsPrimaryKey() {
        assertTrue(pipelineTableMetaData.getColumnMetaData(1).isUniqueKey());
    }
    
    @Test
    void assertIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> pipelineTableMetaData.getColumnMetaData(2));
    }
}
