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

package org.apache.shardingsphere.database.connector.core.metadata.data.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableMetaDataTest {
    
    private TableMetaData tableMetaData;
    
    @BeforeEach
    void setUp() {
        tableMetaData = new TableMetaData(null, Collections.singletonList(new ColumnMetaData("test", Types.INTEGER, true, false, "varchar", true, true, false, false)),
                Collections.emptyList(), Collections.emptyList());
    }
    
    @Test
    void assertGetColumnMetaData() {
        ColumnMetaData actual = tableMetaData.getColumns().iterator().next();
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertThat(actual.getTypeName(), is("varchar"));
        assertTrue(actual.isPrimaryKey());
        assertFalse(actual.isGenerated());
        assertTrue(actual.isCaseSensitive());
        assertFalse(actual.isNullable());
    }
}
