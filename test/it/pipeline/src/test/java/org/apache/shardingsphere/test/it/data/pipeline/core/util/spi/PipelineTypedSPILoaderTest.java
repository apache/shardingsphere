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

package org.apache.shardingsphere.test.it.data.pipeline.core.util.spi;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineTypedSPILoaderTest {
    
    @Test
    void assertFindDatabaseTypedService() {
        Optional<PipelineSQLBuilder> actual = PipelineTypedSPILoader.findDatabaseTypedService(PipelineSQLBuilder.class, "MariaDB");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is("MySQL"));
    }
    
    @Test
    void assertGetPipelineSQLBuilder() {
        PipelineSQLBuilder actual = PipelineTypedSPILoader.getDatabaseTypedService(PipelineSQLBuilder.class, "MariaDB");
        assertNotNull(actual);
        assertThat(actual.getType(), is("MySQL"));
    }
    
    @Test
    void assertFindColumnValueReaderByUnknown() {
        Optional<ColumnValueReader> actual = PipelineTypedSPILoader.findDatabaseTypedService(ColumnValueReader.class, "Unknown");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetColumnValueReaderByBranchDB() {
        ColumnValueReader actual = PipelineTypedSPILoader.getDatabaseTypedService(ColumnValueReader.class, "MariaDB");
        assertNotNull(actual);
        assertThat(actual.getClass().getName(), is(MySQLColumnValueReader.class.getName()));
    }
}
