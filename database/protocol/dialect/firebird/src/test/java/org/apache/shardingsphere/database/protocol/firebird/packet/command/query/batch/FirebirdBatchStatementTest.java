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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBatchStatementTest {
    
    @Test
    void assertAddParameterValues() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.addParameterValues(Arrays.asList("foo", 1L));
        assertThat(batchStatement.getStatementHandle(), is(32));
        assertThat(batchStatement.getParameterValues().size(), is(1));
        assertThat(batchStatement.getParameterValues().get(0), is(Arrays.asList("foo", 1L)));
    }
    
    @Test
    void assertClearParameterValues() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(100);
        batchStatement.addParameterValues(Collections.singletonList("foo"));
        batchStatement.clearParameterValues();
        assertTrue(batchStatement.getParameterValues().isEmpty());
    }
}
