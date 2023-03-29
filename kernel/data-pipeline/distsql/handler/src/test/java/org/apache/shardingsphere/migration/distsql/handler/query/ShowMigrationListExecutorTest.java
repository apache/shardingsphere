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

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShowMigrationListExecutorTest {
    
    @Test
    void assertGetColumnNames() {
        ShowMigrationListExecutor executor = new ShowMigrationListExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(6));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("id"));
        assertThat(iterator.next(), is("tables"));
        assertThat(iterator.next(), is("job_item_count"));
        assertThat(iterator.next(), is("active"));
        assertThat(iterator.next(), is("create_time"));
        assertThat(iterator.next(), is("stop_time"));
    }
}
