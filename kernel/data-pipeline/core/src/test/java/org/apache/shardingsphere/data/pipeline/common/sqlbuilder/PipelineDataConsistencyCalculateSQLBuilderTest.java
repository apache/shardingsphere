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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineDataConsistencyCalculateSQLBuilderTest {
    
    private final PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder = new PipelineDataConsistencyCalculateSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @Test
    void assertBuildQueryAllOrderingSQLFirstQuery() {
        String actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Collections.singletonList("*"), "order_id", true);
        assertThat(actual, is("SELECT * FROM t_order ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id", true);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildQueryAllOrderingSQLNonFirstQuery() {
        String actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Collections.singletonList("*"), "order_id", false);
        assertThat(actual, is("SELECT * FROM t_order WHERE order_id>? ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id", false);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC"));
    }
}
