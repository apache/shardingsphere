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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.handler.type.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.ShowServiceProviderImplementationsStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowServiceProviderImplementationsExecutorTest {
    
    @Test
    void assertGetRowData() {
        QueryableRALExecutor<ShowServiceProviderImplementationsStatement> executor = new ShowServiceProviderImplementationsExecutor();
        ShowServiceProviderImplementationsStatement statement = mock(ShowServiceProviderImplementationsStatement.class);
        when(statement.getServiceProviderInterface()).thenReturn("org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolActiveDetector");
        Collection<LocalDataQueryResultRow> actual = executor.getRows(statement, mock(ShardingSphereMetaData.class));
        assertFalse(actual.isEmpty());
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("HikariDataSourcePoolActiveDetector"));
        assertThat(row.getCell(2), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(row.getCell(3), is("org.apache.shardingsphere.infra.datasource.pool.hikari.detector.HikariDataSourcePoolActiveDetector"));
    }
}
