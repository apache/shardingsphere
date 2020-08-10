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

package org.apache.shardingsphere.shadow.route.engine.impl;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PreparedShadowDataSourceRouterTest {
    
    @Test
    public void isShadowSQL() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonMap("ds", "shadow_ds"));
        ShadowRule shadowRule = new ShadowRule(shadowRuleConfiguration);
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0,
                Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("shadow"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        InsertStatementContext insertStatementContext = new InsertStatementContext(schemaMetaData, Arrays.asList(1, "Tom", 2, "Jerry", 3, true), insertStatement);
        PreparedShadowDataSourceRouter preparedShadowDataSourceRouter = new PreparedShadowDataSourceRouter(shadowRule, insertStatementContext, Arrays.asList(1, "Tom", true));
        assertTrue("should be shadow", preparedShadowDataSourceRouter.isShadowSQL());
    }
}
