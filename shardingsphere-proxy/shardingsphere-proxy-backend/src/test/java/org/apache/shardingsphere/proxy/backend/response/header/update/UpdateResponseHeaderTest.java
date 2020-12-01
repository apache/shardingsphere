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

package org.apache.shardingsphere.proxy.backend.response.header.update;

import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class UpdateResponseHeaderTest {

    @Test
    public void assertPropertiesWhenExecuteResultOfEmptyList() {
        UpdateResponseHeader updateResponseHeader = new UpdateResponseHeader(mock(SQLStatement.class));
        assertThat(updateResponseHeader.getLastInsertId(), is(0L));
        //before mergeUpdateCount
        assertThat(updateResponseHeader.getUpdateCount(), is(0L));

        updateResponseHeader.mergeUpdateCount();

        //after mergeUpdateCount
        assertThat(updateResponseHeader.getUpdateCount(), is(0L));
    }

    @Test
    public void assertPropertiesWhenExecuteResultOfNotEmptyList() {
        UpdateResponseHeader updateResponseHeader = new UpdateResponseHeader(mock(SQLStatement.class), getExecuteUpdateResults());
        assertThat(updateResponseHeader.getLastInsertId(), is(4L));
        //before mergeUpdateCount
        assertThat(updateResponseHeader.getUpdateCount(), is(1L));

        updateResponseHeader.mergeUpdateCount();

        //after mergeUpdateCount
        assertThat(updateResponseHeader.getUpdateCount(), is(4L));
    }

    private Collection<ExecuteResult> getExecuteUpdateResults() {
        UpdateResult updateResult1 = new UpdateResult(1,2L);
        UpdateResult updateResult2 = new UpdateResult(3,4L);

        return Arrays.asList(updateResult1, updateResult2);
    }
}
