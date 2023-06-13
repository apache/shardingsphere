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

package org.apache.shardingsphere.data.pipeline.postgresql.ddlgenerator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PostgreSQLColumnTypeTest {
    
    @Test
    void assertValueOfZeroValue() {
        assertThat(PostgreSQLColumnType.valueOf(0L), is(PostgreSQLColumnType.UNKNOWN));
    }
    
    @Test
    void assertValueOfFoundValue() {
        assertThat(PostgreSQLColumnType.valueOf(1231L), is(PostgreSQLColumnType.NUMERIC));
    }
    
    @Test
    void assertValueOfNotExistedValue() {
        assertThat(PostgreSQLColumnType.valueOf(1L), is(PostgreSQLColumnType.UNKNOWN));
    }
}
