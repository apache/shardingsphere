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
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableAndConditionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableJoinExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;

/**
 * Extract table result.
 *
 * @author duhongjun
 */
public final class FromClauseExtractHandler implements ASTExtractHandler<Optional<TableAndConditionExtractResult>> {
    
    private final TableNameExtractHandler tableNameExtractHandler = new TableNameExtractHandler();
    
    private ConditionExtractHandler conditionExtractHandler;
    
    @Override
    public Optional<TableAndConditionExtractResult> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> fromNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ASTUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        TableAndConditionExtractResult result = new TableAndConditionExtractResult();
        conditionExtractHandler = new ConditionExtractHandler(result.getTableAliases());
        Collection<ParserRuleContext> questionNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION);
        result.setParamenterCount(questionNodes.size());
        Map<ParserRuleContext, Integer> questionNodeIndexMap = new HashMap<>();
        int index = 0;
        for(ParserRuleContext each : questionNodes) {
            questionNodeIndexMap.put(each, index++);
        }
        extractAndFillTableResult(result, tableReferenceNodes, questionNodeIndexMap);
        extractAndFillWhere(result, questionNodeIndexMap, ancestorNode);
        return Optional.of(result);
    }
    
    private void extractAndFillTableResult(final TableAndConditionExtractResult tableAndConditionExtractResult, final Collection<ParserRuleContext> tableReferenceNodes, Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        for (ParserRuleContext each : tableReferenceNodes) {
            Optional<ParserRuleContext> joinTableNode = ASTUtils.findFirstChildNode(each, RuleName.JOIN_TABLE);
            Optional<ParserRuleContext> tableFactorNode = null;
            if (joinTableNode.isPresent()) {
                tableFactorNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.TABLE_FACTOR);
            } else {
                tableFactorNode = ASTUtils.findFirstChildNode(each, RuleName.TABLE_FACTOR);
            }
            //TODO subquery
            if (!tableFactorNode.isPresent()) {
                continue;
            }
            Optional<TableExtractResult> extractResult = tableNameExtractHandler.extract(tableFactorNode.get());
            if (!extractResult.isPresent()) {
                continue;
            }
            if (!joinTableNode.isPresent()) {
                fillTableResult(tableAndConditionExtractResult, extractResult.get());
                continue;
            }
            Optional<ParserRuleContext> joinConditionNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.JOIN_CONDITION);
            if (joinConditionNode.isPresent()) {
                Optional<OrCondition> conditionResult = buildCondition(joinConditionNode.get(), questionNodeIndexMap, tableAndConditionExtractResult.getTableAliases());
                if(conditionResult.isPresent()) {
                    TableJoinExtractResult tableJoinResult = new TableJoinExtractResult(extractResult.get());
                    tableJoinResult.getJoinConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
                    fillTableResult(tableAndConditionExtractResult, tableJoinResult);
                    continue;
                }
            }
            fillTableResult(tableAndConditionExtractResult, extractResult.get());
        }
    }
    
    private void fillTableResult(final TableAndConditionExtractResult tableAndConditionExtractResult, final TableExtractResult extractResult) {
        String alias = extractResult.getName();
        if(extractResult.getAlias().isPresent()) {
            alias = extractResult.getAlias().get();
        }
        tableAndConditionExtractResult.getTableAliases().put(alias, extractResult.getName());
    }
    
    private Optional<OrCondition> buildCondition(final ParserRuleContext node, Map<ParserRuleContext, Integer> questionNodeIndexMap, Map<String,String> tableAliases) {
        Optional<ParserRuleContext> exprNode = ASTUtils.findFirstChildNode(node, RuleName.EXPR);
        if (exprNode.isPresent()) {
            return conditionExtractHandler.extractCondition(questionNodeIndexMap, exprNode.get());
        }
        return Optional.absent();
    }
    
    private void extractAndFillWhere(final TableAndConditionExtractResult tableAndConditionExtractResult, Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.WHERECLAUSE);
        if(!whereNode.isPresent()) {
            return;
        }
        Optional<OrCondition> conditions = buildCondition((ParserRuleContext)whereNode.get().getChild(1), questionNodeIndexMap, tableAndConditionExtractResult.getTableAliases());
        if(conditions.isPresent()) {
            tableAndConditionExtractResult.getConditions().getAndConditions().addAll(conditions.get().getAndConditions());
        }
    }
}
