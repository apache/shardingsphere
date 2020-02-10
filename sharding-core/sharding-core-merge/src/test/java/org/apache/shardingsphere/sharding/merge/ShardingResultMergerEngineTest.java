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

package org.apache.shardingsphere.sharding.merge;

import org.apache.shardingsphere.sharding.merge.dal.ShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.underlying.merge.engine.merger.impl.TransparentResultMerger;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ShardingResultMergerEngineTest {
    
    @Test
    public void assertNewInstanceWithSelectStatement() {
        ShardingSphereProperties properties = new ShardingSphereProperties(new Properties());
        SQLStatementContext sqlStatementContext = new SelectSQLStatementContext(new SelectStatement(),
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, properties, sqlStatementContext), instanceOf(ShardingDQLResultMerger.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() {
        ShardingSphereProperties properties = new ShardingSphereProperties(new Properties());
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new ShowStatement());
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, properties, sqlStatementContext), instanceOf(ShardingDALResultMerger.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0);
        insertColumnsSegment.getColumns().add(new ColumnSegment(0, 0, "col"));
        insertStatement.setColumns(insertColumnsSegment);
        SQLStatementContext sqlStatementContext = new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement);
        ShardingSphereProperties properties = new ShardingSphereProperties(new Properties());
        assertThat(new ShardingResultMergerEngine().newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, properties, sqlStatementContext), instanceOf(TransparentResultMerger.class));
    }
}
