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

package org.apache.shardingsphere.migration.distsql.handler.query;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShowMigrationCheckStatusExecutorTest {
    
    private final ShowMigrationCheckStatusExecutor executor = new ShowMigrationCheckStatusExecutor();
    
    @Test
    public void assertGetColumnNames() {
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(9));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("tables"));
        assertThat(iterator.next(), is("result"));
        assertThat(iterator.next(), is("check_failed_tables"));
        assertThat(iterator.next(), is("finished_percentage"));
        assertThat(iterator.next(), is("remaining_seconds"));
        assertThat(iterator.next(), is("check_begin_time"));
        assertThat(iterator.next(), is("check_end_time"));
        assertThat(iterator.next(), is("duration_seconds"));
        assertThat(iterator.next(), is("error_message"));
    }
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    public void assertConvert() {
        ConsistencyCheckJobItemInfo jobItemInfo = new ConsistencyCheckJobItemInfo();
        jobItemInfo.setTableNames("t_order");
        jobItemInfo.setCheckSuccess(true);
        jobItemInfo.setCheckFailedTableNames(null);
        jobItemInfo.setFinishedPercentage(100);
        jobItemInfo.setRemainingSeconds(0);
        jobItemInfo.setCheckBeginTime("");
        jobItemInfo.setCheckEndTime("");
        jobItemInfo.setDurationSeconds(1);
        LocalDataQueryResultRow row = executor.convert(jobItemInfo);
        List<Object> actual = (List<Object>) Plugins.getMemberAccessor().get(LocalDataQueryResultRow.class.getDeclaredField("data"), row);
        assertThat(actual.size(), is(executor.getColumnNames().size()));
    }
}
