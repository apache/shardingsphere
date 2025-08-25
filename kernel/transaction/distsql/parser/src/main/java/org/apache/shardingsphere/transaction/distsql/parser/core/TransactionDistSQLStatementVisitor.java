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

package org.apache.shardingsphere.transaction.distsql.parser.core;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.AlterTransactionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.ProviderDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.ShowTransactionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.TransactionDistSQLStatementParser.TransactionRuleDefinitionContext;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.transaction.distsql.segment.TransactionProviderSegment;
import org.apache.shardingsphere.transaction.distsql.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.distsql.statement.updatable.AlterTransactionRuleStatement;

import java.util.Properties;

/**
 * SQL statement visitor for transaction DistSQL.
 */
public final class TransactionDistSQLStatementVisitor extends TransactionDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowTransactionRule(final ShowTransactionRuleContext ctx) {
        return new ShowTransactionRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterTransactionRule(final AlterTransactionRuleContext ctx) {
        return visit(ctx.transactionRuleDefinition());
    }
    
    @Override
    public ASTNode visitTransactionRuleDefinition(final TransactionRuleDefinitionContext ctx) {
        return null == ctx.providerDefinition() ? new AlterTransactionRuleStatement(IdentifierValueUtils.getValue(ctx.defaultType()), new TransactionProviderSegment(null, null))
                : new AlterTransactionRuleStatement(IdentifierValueUtils.getValue(ctx.defaultType()), (TransactionProviderSegment) visit(ctx.providerDefinition()));
    }
    
    @Override
    public ASTNode visitProviderDefinition(final ProviderDefinitionContext ctx) {
        return new TransactionProviderSegment(IdentifierValueUtils.getValue(ctx.providerName()), getProperties(ctx.propertiesDefinition()));
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
}
