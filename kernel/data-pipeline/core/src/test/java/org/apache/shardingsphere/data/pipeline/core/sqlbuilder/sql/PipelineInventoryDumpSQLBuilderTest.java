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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineInventoryDumpSQLBuilderTest {
    
    private final PipelineInventoryDumpSQLBuilder sqlBuilder = new PipelineInventoryDumpSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @Test
    void assertBuildFetchAllSQL() {
        String actual = sqlBuilder.buildFetchAllSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"));
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order"));
        actual = sqlBuilder.buildFetchAllSQL(null, "t_order", Collections.singletonList("*"));
        assertThat(actual, is("SELECT * FROM t_order"));
    }
}
