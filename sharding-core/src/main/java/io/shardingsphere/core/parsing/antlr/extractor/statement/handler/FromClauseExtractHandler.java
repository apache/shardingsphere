/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */


package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import java.util.Collection;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.FromClauseExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableJoinExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.token.TableToken;

/**
 * Extract table result.
 * 
 * @author duhongjun
 */
public class FromClauseExtractHandler implements ASTExtractHandler {

    @Override
    public Optional<ExtractResult> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> fromNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.FROM_CLAUSE);
        if(!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ASTUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if(tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        FromClauseExtractResult result = new FromClauseExtractResult();
        for(ParserRuleContext each : tableReferenceNodes){
            Optional<ParserRuleContext> joinTableNode = ASTUtils.findFirstChildNode(each, RuleName.JOIN_TABLE);
            Optional<ParserRuleContext> tableFactorNode = null;
            if(joinTableNode.isPresent()) {
                tableFactorNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.TABLE_FACTOR);
            }else {
                tableFactorNode = ASTUtils.findFirstChildNode(each, RuleName.TABLE_FACTOR);
            }
            //TODO subquery
            if(!tableFactorNode.isPresent()) {
                continue;
            }
            TableExtractResult extractResult = fillExtractResult(tableFactorNode.get());
            if(!joinTableNode.isPresent()) {
                result.getTableResults().add(extractResult);
                continue;
            }
            Optional<ParserRuleContext> joinConditionNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.JOIN_CONDITION);
            if(joinConditionNode.isPresent()) {
                Optional<ParserRuleContext> exprNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.EXPR);
                if(exprNode.isPresent()) {
                    TableJoinExtractResult tableJoinResult = new TableJoinExtractResult(extractResult);
                    //TODO extract condition
                    result.getTableResults().add(tableJoinResult);
                    continue;
                }
            }
            result.getTableResults().add(extractResult);
        }
        return Optional.<ExtractResult>of(result);
    }
    
    private TableExtractResult fillExtractResult(ParserRuleContext node) {
        String tableText = node.getText();
        int dotPosition = tableText.contains(Symbol.DOT.getLiterals()) ? tableText.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
        String tableName = tableText;
        String schemaName = "";
        if(0 < dotPosition) {
            tableName = tableText.substring(dotPosition + 1);
            dotPosition = schemaName.contains(Symbol.DOT.getLiterals()) ? schemaName.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
            schemaName = tableText.substring(dotPosition + 1);
        }
        Optional<ParserRuleContext> aliasNode = ASTUtils.findFirstChildNode(node, RuleName.ALIAS);
        String alias = "";
        if(aliasNode.isPresent()) {
            alias = aliasNode.get().getText();
        }
        TableToken tableToken = new TableToken(node.getStart().getStartIndex(), dotPosition, tableText);
        return new TableExtractResult(tableName, alias, schemaName, tableToken);
    }
}
