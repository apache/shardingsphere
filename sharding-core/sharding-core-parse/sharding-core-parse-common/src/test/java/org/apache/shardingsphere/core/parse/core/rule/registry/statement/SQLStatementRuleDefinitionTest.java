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

package org.apache.shardingsphere.core.parse.core.rule.registry.statement;

import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.core.rule.registry.extractor.ExtractorRuleDefinition;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLStatementRuleDefinitionTest {
    
    private static SQLStatementRuleDefinition sqlStatementRuleDefinition;
    
    @BeforeClass
    public static void setUp() {
        ExtractorRuleDefinitionEntity extractorRuleDefinitionEntity = new ExtractorRuleDefinitionEntityLoader().load("META-INF/parsing-rule-definition/extractor-rule-definition.xml");
        sqlStatementRuleDefinition = new SQLStatementRuleDefinition(
                new SQLStatementRuleDefinitionEntityLoader().load("META-INF/parsing-rule-definition/mysql/sql-statement-rule-definition.xml"), 
                new ExtractorRuleDefinition(extractorRuleDefinitionEntity));
    }
    
    @Test
    public void assertSelectStatementRule() {
        SQLStatementRule sqlStatementRule = sqlStatementRuleDefinition.getSQLStatementRule("SelectContext");
        assertThat(sqlStatementRule.getContextName(), is("select"));
        assertThat(sqlStatementRule.getSqlStatementClass().getName(), is("org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement"));
        assertThat(sqlStatementRule.getExtractors().size(), is(3));
    }
    
    @Test
    public void assertDeleteStatementRule() {
        SQLStatementRule sqlStatementRule = sqlStatementRuleDefinition.getSQLStatementRule("DeleteContext");
        assertThat(sqlStatementRule.getExtractors().size(), is(0));
    }
}
