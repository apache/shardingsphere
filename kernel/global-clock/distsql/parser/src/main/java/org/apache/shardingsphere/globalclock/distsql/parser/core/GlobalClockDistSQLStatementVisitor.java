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

package org.apache.shardingsphere.globalclock.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementParser.AlterGlobalClockRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementParser.GlobalClockRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.GlobalClockDistSQLStatementParser.ShowGlobalClockRuleContext;
import org.apache.shardingsphere.globalclock.distsql.statement.queryable.ShowGlobalClockRuleStatement;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Properties;

/**
 * SQL statement visitor for global clock DistSQL.
 */
public final class GlobalClockDistSQLStatementVisitor extends GlobalClockDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowGlobalClockRule(final ShowGlobalClockRuleContext ctx) {
        return new ShowGlobalClockRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterGlobalClockRule(final AlterGlobalClockRuleContext ctx) {
        GlobalClockRuleDefinitionContext ruleDefinitionContext = ctx.globalClockRuleDefinition();
        return new AlterGlobalClockRuleStatement(getIdentifierValue(ruleDefinitionContext.typeDefinition().typeName()), getIdentifierValue(ruleDefinitionContext.providerDefinition().providerName()),
                Boolean.parseBoolean(getIdentifierValue(ruleDefinitionContext.enabledDefinition().enabled())), getProperties(ruleDefinitionContext.propertiesDefinition()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
}
