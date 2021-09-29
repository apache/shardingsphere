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

package org.apache.shardingsphere.infra.context.refresher;

import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataRefresherFactoryTest {
    
    @Test
    public void assertNewInstanceWithCreateTableStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(CreateTableStatement.class)).size(), is(2));
    }
    
    @Test
    public void assertNewInstanceWithAlterTableStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(AlterTableStatement.class)).size(), is(2));
    }
    
    @Test
    public void assertNewInstanceWithDropTableStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(DropTableStatement.class)).size(), is(2));
    }
    
    @Test
    public void assertNewInstanceWithCreateIndexStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(CreateIndexStatement.class)).size(), is(1));
    }
    
    @Test
    public void assertNewInstanceWithDropIndexStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(DropIndexStatement.class)).size(), is(1));
    }
    
    @Test
    public void assertNewInstanceWithAlterIndexStatement() {
        assertThat(MetaDataRefresherFactory.newInstance(mock(AlterIndexStatement.class)).size(), is(1));
    }
}
