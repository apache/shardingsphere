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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane;

import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DefaultSaneQueryResultEngineTest {
    
    @Test
    public void assertGetSaneQueryResultForOtherStatement() {
        assertThat(new DefaultSaneQueryResultEngine().getSaneQueryResult(() -> 0, null), is(Optional.empty()));
    }
    
    @Test
    public void assertGetSaneQueryResultForSelectStatement() {
        Optional<ExecuteResult> actual = new DefaultSaneQueryResultEngine().getSaneQueryResult(new SelectStatement() {
        }, null);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(RawMemoryQueryResult.class));
        RawMemoryQueryResult actualResult = (RawMemoryQueryResult) actual.get();
        assertThat(actualResult.getRowCount(), is(1L));
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is("1"));
        assertFalse(actualResult.next());
    }
}
