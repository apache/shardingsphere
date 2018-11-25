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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableAndConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableJoinSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableSegment;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * From clause extractor.
 *
 * @author duhongjun
 */
public final class FromClauseExtractor implements OptionalSQLSegmentExtractor {
    
    private final TableNameExtractor tableNameExtractor = new TableNameExtractor();
    
    private ConditionExtractor conditionExtractor;
    
    @Override
    public Optional<TableAndConditionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> fromNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ASTUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        TableAndConditionSegment result = new TableAndConditionSegment();
        conditionExtractor = new ConditionExtractor(result.getTableAliases());
        Collection<ParserRuleContext> questionNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION);
        result.setParamenterCount(questionNodes.size());
        Map<ParserRuleContext, Integer> questionNodeIndexMap = new HashMap<>();
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            questionNodeIndexMap.put(each, index++);
        }
        extractAndFillTableResult(result, tableReferenceNodes, questionNodeIndexMap);
        extractAndFillWhere(result, questionNodeIndexMap, ancestorNode);
        return Optional.of(result);
    }
    
    private void extractAndFillTableResult(final TableAndConditionSegment tableAndConditionSegment, final Collection<ParserRuleContext> tableReferenceNodes,
                                           final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        for (ParserRuleContext each : tableReferenceNodes) {
            Optional<ParserRuleContext> joinTableNode = ASTUtils.findFirstChildNode(each, RuleName.JOIN_TABLE);
            Optional<ParserRuleContext> tableFactorNode = joinTableNode.isPresent()
                    ? ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.TABLE_FACTOR) : ASTUtils.findFirstChildNode(each, RuleName.TABLE_FACTOR);
            //TODO subquery
            if (!tableFactorNode.isPresent()) {
                continue;
            }
            Optional<TableSegment> extractResult = tableNameExtractor.extract(tableFactorNode.get());
            if (!extractResult.isPresent()) {
                continue;
            }
            if (!joinTableNode.isPresent()) {
                fillTableResult(tableAndConditionSegment, extractResult.get());
                continue;
            }
            Optional<ParserRuleContext> joinConditionNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.JOIN_CONDITION);
            if (joinConditionNode.isPresent()) {
                Optional<OrCondition> conditionResult = buildCondition(joinConditionNode.get(), questionNodeIndexMap, tableAndConditionSegment.getTableAliases());
                if (conditionResult.isPresent()) {
                    TableJoinSegment tableJoinResult = new TableJoinSegment(extractResult.get());
                    tableJoinResult.getJoinConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
                    fillTableResult(tableAndConditionSegment, tableJoinResult);
                    continue;
                }
            }
            fillTableResult(tableAndConditionSegment, extractResult.get());
        }
    }
    
    private void fillTableResult(final TableAndConditionSegment tableAndConditionSegment, final TableSegment tableSegment) {
        String alias = tableSegment.getName();
        if (tableSegment.getAlias().isPresent()) {
            alias = tableSegment.getAlias().get();
        }
        tableAndConditionSegment.getTableAliases().put(alias, tableSegment.getName());
    }
    
    private Optional<OrCondition> buildCondition(final ParserRuleContext node, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final Map<String, String> tableAliases) {
        Optional<ParserRuleContext> exprNode = ASTUtils.findFirstChildNode(node, RuleName.EXPR);
        if (exprNode.isPresent()) {
            return conditionExtractor.extractCondition(questionNodeIndexMap, exprNode.get());
        }
        return Optional.absent();
    }
    
    private void extractAndFillWhere(final TableAndConditionSegment tableAndConditionSegment, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return;
        }
        Optional<OrCondition> conditions = buildCondition((ParserRuleContext) whereNode.get().getChild(1), questionNodeIndexMap, tableAndConditionSegment.getTableAliases());
        if (conditions.isPresent()) {
            tableAndConditionSegment.getConditions().getAndConditions().addAll(conditions.get().getAndConditions());
        }
    }
}
