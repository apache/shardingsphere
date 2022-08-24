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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLColumnValueReader;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ColumnValueReaderFactoryTest {
    
    @Test
    public void assertGetInstance() {
        Collection<Pair<String, Class<? extends ColumnValueReader>>> paramResult = Arrays.asList(
                Pair.of("MySQL", MySQLColumnValueReader.class),
                Pair.of("PostgreSQL", PostgreSQLColumnValueReader.class), Pair.of("openGauss", PostgreSQLColumnValueReader.class));
        for (Pair<String, Class<? extends ColumnValueReader>> each : paramResult) {
            ColumnValueReader actual = ColumnValueReaderFactory.getInstance(each.getKey());
            assertThat(actual, instanceOf(each.getValue()));
        }
    }
}
