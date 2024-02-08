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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowShardingAlgorithmPluginsResultRowBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShowShardingAlgorithmPluginsResultRowBuilderTest {
    
    @Test
    void assertGetRowData() {
        ShowShardingAlgorithmPluginsResultRowBuilder rowBuilder = new ShowShardingAlgorithmPluginsResultRowBuilder();
        Collection<LocalDataQueryResultRow> actual = rowBuilder.generateRows();
        assertThat(actual.size(), is(15));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("FOO.DISTSQL.FIXTURE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("BAR.DISTSQL.FIXTURE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("CORE.HINT.FIXTURE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("CORE.AUTO.FIXTURE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("CORE.STANDARD.FIXTURE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("MOD"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("HASH_MOD"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("VOLUME_RANGE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("BOUNDARY_RANGE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("AUTO_INTERVAL"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("INTERVAL"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("CLASS_BASED"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("INLINE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("COMPLEX_INLINE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("HINT_INLINE"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
    }
}
