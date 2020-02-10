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

package org.apache.shardingsphere.shadow.rewrite.judgement.impl;

import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreparedJudgementEngineTest {
    
    @Test
    public void isShadowSql() {
        RelationMetas relationMetas = mock(RelationMetas.class);
        when(relationMetas.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "shadow"));
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration();
        shadowRuleConfiguration.setColumn("shadow");
        ShadowRule shadowRule = new ShadowRule(shadowRuleConfiguration);
        InsertStatement insertStatement = new InsertStatement();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0);
        insertColumnsSegment.getColumns().addAll(Arrays.asList(new ColumnSegment(0, 0, "id"),
                new ColumnSegment(0, 0, "name"),
                new ColumnSegment(0, 0, "shadow")));
        insertStatement.setColumns(insertColumnsSegment);
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(relationMetas, Arrays.<Object>asList(1, "Tom", 2, "Jerry", 3, true), insertStatement);
        PreparedJudgementEngine preparedJudgementEngine = new PreparedJudgementEngine(shadowRule, insertSQLStatementContext, Arrays.<Object>asList(1, "Tom", true));
        Assert.assertTrue("should be shadow", preparedJudgementEngine.isShadowSQL());
    }
}
