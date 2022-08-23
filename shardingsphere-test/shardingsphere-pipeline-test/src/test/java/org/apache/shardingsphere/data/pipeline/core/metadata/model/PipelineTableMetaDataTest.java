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

import org.apache.shardingsphere.data.pipeline.api.metadata.PipelineColumnMetaData;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PipelineTableMetaDataTest {
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    @Before
    public void setUp() {
        PipelineColumnMetaData column = new PipelineColumnMetaData(1, "test", Types.INTEGER, "INTEGER", true, true);
        pipelineTableMetaData = new PipelineTableMetaData("test_data", Collections.singletonMap("test", column), Collections.emptySet());
    }
    
    @Test
    public void assertGetColumnMetaDataGivenColumnIndex() {
        PipelineColumnMetaData actual = pipelineTableMetaData.getColumnMetaData(0);
        assertThat(actual.getOrdinalPosition(), is(1));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    public void assertGetColumnMetaDataGivenColumnName() {
        PipelineColumnMetaData actual = pipelineTableMetaData.getColumnMetaData("test");
        assertNull(pipelineTableMetaData.getColumnMetaData("non_exist"));
        assertThat(actual.getOrdinalPosition(), is(1));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    public void assertIsPrimaryKey() {
        assertTrue(pipelineTableMetaData.isUniqueKey(0));
        assertFalse(pipelineTableMetaData.isUniqueKey(1));
    }
}
