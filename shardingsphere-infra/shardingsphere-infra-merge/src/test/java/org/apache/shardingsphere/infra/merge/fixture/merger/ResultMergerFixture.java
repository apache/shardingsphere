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

package org.apache.shardingsphere.infra.merge.fixture.merger;

import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.StreamJDBCQueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ResultMergerFixture implements ResultMerger {
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext, final ShardingSphereSchema schema) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("merged_value");
        QueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        return new TransparentMergedResult(queryResult);
    }
}
