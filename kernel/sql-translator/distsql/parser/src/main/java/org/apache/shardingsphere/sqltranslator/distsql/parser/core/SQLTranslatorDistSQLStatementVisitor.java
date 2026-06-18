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

package org.apache.shardingsphere.sqltranslator.distsql.parser.core;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.AlterSQLTranslatorRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.ShowSQLTranslatorRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLTranslatorDistSQLStatementParser.UseOriginalSQLDefinitionContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sqltranslator.distsql.statement.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;

import java.util.Properties;

/**
 * SQL statement visitor for SQL translator DistSQL.
 */
public final class SQLTranslatorDistSQLStatementVisitor extends SQLTranslatorDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowSQLTranslatorRule(final ShowSQLTranslatorRuleContext ctx) {
        return new ShowSQLTranslatorRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterSQLTranslatorRule(final AlterSQLTranslatorRuleContext ctx) {
        return new AlterSQLTranslatorRuleStatement((AlgorithmSegment) visit(ctx.sqlTranslatorRuleDefinition().algorithmDefinition()),
                isUseOriginalSQLWhenTranslatingFailed(ctx.sqlTranslatorRuleDefinition().useOriginalSQLDefinition()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(IdentifierValueUtils.getValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    private Boolean isUseOriginalSQLWhenTranslatingFailed(final UseOriginalSQLDefinitionContext ctx) {
        return null == ctx ? null : Boolean.valueOf(IdentifierValueUtils.getValue(ctx.useOriginalSQL()));
    }
}
