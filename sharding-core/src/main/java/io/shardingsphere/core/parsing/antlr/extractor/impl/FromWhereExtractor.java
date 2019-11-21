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

package io.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableJoinSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * From clause extractor.
 *
 * @author duhongjun
 */
public final class FromWhereExtractor implements OptionalSQLSegmentExtractor {
    
    private final TableNameExtractor tableNameExtractor = new TableNameExtractor();
    
    private PredicateExtractor predicateSegmentExtractor;
    
    @Override
    public Optional<FromWhereSegment> extract(final ParserRuleContext ancestorNode) {
        return extract(ancestorNode, ancestorNode);
    }
    
    /**
     * Extract SQL segment from SQL AST.
     *
     * @param ancestorNode ancestor node of AST
     * @param rootNode root node of AST
     * @return SQL segment
     */
    public Optional<FromWhereSegment> extract(final ParserRuleContext ancestorNode, final ParserRuleContext rootNode) {
        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> fromNode = ExtractorUtils.findFirstChildNodeNoneRecursive(selectClauseNode.get().getParent(), RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ExtractorUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        FromWhereSegment result = new FromWhereSegment();
        predicateSegmentExtractor = new PredicateExtractor(result.getTableAliases());
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(rootNode, RuleName.QUESTION);
        result.setParameterCount(questionNodes.size());
        Map<ParserRuleContext, Integer> questionNodeIndexMap = getPlaceholderAndNodeIndexMap(questionNodes);
        extractAndFillTableSegment(result, tableReferenceNodes, questionNodeIndexMap);
        extractAndFillWhere(result, questionNodeIndexMap, fromNode.get().getParent());
        return Optional.of(result);
    }
    
    private Map<ParserRuleContext, Integer> getPlaceholderAndNodeIndexMap(final Collection<ParserRuleContext> questionNodes) {
        Map<ParserRuleContext, Integer> result = new HashMap<>(questionNodes.size(), 1);
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            result.put(each, index++);
        }
        return result;
    }
    
    private void extractAndFillTableSegment(final FromWhereSegment fromWhereSegment,
                                            final Collection<ParserRuleContext> tableReferenceNodes, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        for (ParserRuleContext each : tableReferenceNodes) {
            for (int i = 0; i < each.getChildCount(); i++) {
                if (each.getChild(i) instanceof TerminalNode) {
                    continue;
                }
                ParserRuleContext childNode = (ParserRuleContext) each.getChild(i);
                if (RuleName.TABLE_REFERENCES.getName().equals(childNode.getClass().getSimpleName())) {
                    Collection<ParserRuleContext> subTableReferenceNodes = ExtractorUtils.getAllDescendantNodes(childNode, RuleName.TABLE_REFERENCE);
                    if (!subTableReferenceNodes.isEmpty()) {
                        extractAndFillTableSegment(fromWhereSegment, subTableReferenceNodes, questionNodeIndexMap);
                    }
                    continue;
                }
                fillTable(fromWhereSegment, childNode, questionNodeIndexMap);
            }
        }
    }
    
    private void fillTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext joinOrTableFactorNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        if (!RuleName.JOIN_TABLE.getName().endsWith(joinOrTableFactorNode.getClass().getSimpleName())) {
            Optional<TableSegment> tableSegment = tableNameExtractor.extract(joinOrTableFactorNode);
            Preconditions.checkState(tableSegment.isPresent());
            fillTableResult(fromWhereSegment, tableSegment.get());
        }
        Optional<ParserRuleContext> joinConditionNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.JOIN_CONDITION);
        if (!joinConditionNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> tableFactorNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.TABLE_FACTOR);
        Preconditions.checkState(tableFactorNode.isPresent());
        Optional<TableSegment> tableSegment = tableNameExtractor.extract(tableFactorNode.get());
        Preconditions.checkState(tableSegment.isPresent());
        TableJoinSegment tableJoinResult = new TableJoinSegment(tableSegment.get());
        Optional<OrConditionSegment> conditionResult = buildCondition(joinConditionNode.get(), questionNodeIndexMap);
        if (conditionResult.isPresent()) {
            tableJoinResult.getJoinConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
            fromWhereSegment.getConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
        }
        fillTableResult(fromWhereSegment, tableJoinResult);
    }
    
    private void fillTableResult(final FromWhereSegment fromWhereSegment, final TableSegment tableSegment) {
        String alias = tableSegment.getName();
        if (tableSegment.getAlias().isPresent()) {
            alias = tableSegment.getAlias().get();
        }
        fromWhereSegment.getTableAliases().put(alias, tableSegment.getName());
    }
    
    private void extractAndFillWhere(final FromWhereSegment fromWhereSegment, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return;
        }
        Optional<OrConditionSegment> conditions = buildCondition((ParserRuleContext) whereNode.get().getChild(1), questionNodeIndexMap);
        if (conditions.isPresent()) {
            fromWhereSegment.getConditions().getAndConditions().addAll(conditions.get().getAndConditions());
        }
    }
    
    private Optional<OrConditionSegment> buildCondition(final ParserRuleContext node, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode(node, RuleName.EXPR);
        return exprNode.isPresent() ? predicateSegmentExtractor.extractCondition(questionNodeIndexMap, exprNode.get()) : Optional.<OrConditionSegment>absent();
    }
}
