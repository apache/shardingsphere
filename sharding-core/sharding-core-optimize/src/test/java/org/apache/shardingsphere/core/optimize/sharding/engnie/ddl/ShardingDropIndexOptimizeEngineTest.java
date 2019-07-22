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

package org.apache.shardingsphere.core.optimize.sharding.engnie.ddl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingDropIndexOptimizeEngineTest {
    
    private DropIndexStatement dropIndexStatement;
    
    @Mock
    private ShardingTableMetaData shardingTableMetaData;
    
    @Before
    public void setUp() {
        dropIndexStatement = new DropIndexStatement();
        dropIndexStatement.setIndex(new IndexSegment(0, 0, "idx"));
        when(shardingTableMetaData.getLogicTableName("idx")).thenReturn(Optional.of("meta_tbl"));
    }
    
    @Test
    public void assertOptimizeWithTableName() {
        dropIndexStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        ShardingDropIndexOptimizedStatement actual = new ShardingDropIndexOptimizeEngine(dropIndexStatement, shardingTableMetaData).optimize();
        assertThat(actual.getSQLStatement(), is((SQLStatement) dropIndexStatement));
        assertThat(actual.getTableName(), is("tbl"));
    }
    
    @Test
    public void assertOptimizeWithoutTableName() {
        ShardingDropIndexOptimizedStatement actual = new ShardingDropIndexOptimizeEngine(dropIndexStatement, shardingTableMetaData).optimize();
        assertThat(actual.getSQLStatement(), is((SQLStatement) dropIndexStatement));
        assertThat(actual.getTableName(), is("meta_tbl"));
    }
}
