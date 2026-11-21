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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SQLStatementContextExtractor.class)
class DeleteStatementBaseContextTest {
    
    @Test
    void assertNewInstanceWithDataModification() {
        DatabaseType type = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        WhereSegment childWhere = new WhereSegment(20, 24, new LiteralExpressionSegment(24, 24, true));
        DeleteStatement child = DeleteStatement.builder().databaseType(type).table(new SimpleTableSegment(new TableNameSegment(10, 16, new IdentifierValue("bar_tbl")))).where(childWhere).build();
        CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 40, new AliasSegment(5, 8, new IdentifierValue("foo_changed")), null, child);
        WithSegment with = new WithSegment(0, 40, Collections.singletonList(segment));
        WhereSegment parentWhere = new WhereSegment(60, 64, new LiteralExpressionSegment(64, 64, true));
        DeleteStatement statement = DeleteStatement.builder().databaseType(type)
                .table(new SimpleTableSegment(new TableNameSegment(45, 51, new IdentifierValue("foo_tbl")))).with(with).where(parentWhere).build();
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(SQLStatementContextExtractor.createDataModificationBaseContexts(with.getCommonTableExpressions(), metaData, "foo_db")).thenReturn(Collections.emptyList());
        DeleteStatementBaseContext actual = new DeleteStatementBaseContext(statement, metaData, "foo_db");
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(parentWhere)));
    }
}
