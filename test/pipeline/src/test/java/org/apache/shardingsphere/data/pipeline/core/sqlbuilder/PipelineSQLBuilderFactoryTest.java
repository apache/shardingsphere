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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder.MySQLPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.opengauss.sqlbuilder.OpenGaussPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.PostgreSQLPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineSQLBuilderFactoryTest {
    
    @Test
    public void assertGetInstance() {
        Collection<Pair<String, Class<? extends PipelineSQLBuilder>>> paramResult = Arrays.asList(
                Pair.of("MySQL", MySQLPipelineSQLBuilder.class), Pair.of("PostgreSQL", PostgreSQLPipelineSQLBuilder.class),
                Pair.of("openGauss", OpenGaussPipelineSQLBuilder.class),
                Pair.of("DB2", DefaultPipelineSQLBuilder.class));
        for (Pair<String, Class<? extends PipelineSQLBuilder>> each : paramResult) {
            PipelineSQLBuilder actual = PipelineSQLBuilderFactory.getInstance(each.getKey());
            assertThat(actual, instanceOf(each.getValue()));
        }
    }
}
