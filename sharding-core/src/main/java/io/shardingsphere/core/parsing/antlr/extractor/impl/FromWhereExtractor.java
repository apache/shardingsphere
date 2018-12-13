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
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
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
        Optional<ParserRuleContext> fromNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ExtractorUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        FromWhereSegment result = new FromWhereSegment();
        predicateSegmentExtractor = new PredicateExtractor(result.getTableAliases());
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION);
        result.setParameterCount(questionNodes.size());
        Map<ParserRuleContext, Integer> questionNodeIndexMap = new HashMap<>();
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            questionNodeIndexMap.put(each, index++);
        }
        extractAndFillTableSegment(result, tableReferenceNodes, questionNodeIndexMap);
        extractAndFillWhere(result, questionNodeIndexMap, fromNode.get().getParent());
        return Optional.of(result);
    }
    
    private void extractAndFillTableSegment(final FromWhereSegment fromWhereSegment, final Collection<ParserRuleContext> tableReferenceNodes,
                                            final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        for (ParserRuleContext each : tableReferenceNodes) {
            for (int i = 0; i < each.getChildCount(); i++) {
                if (each.getChild(i) instanceof TerminalNode) {
                    continue;
                }
                ParserRuleContext childNode = (ParserRuleContext) each.getChild(i);
                if (RuleName.TABLE_REFERENCES.getName().equals(childNode.getClass().getSimpleName())) {
                    final Collection<ParserRuleContext> subTableReferenceNodes = ExtractorUtils.getAllDescendantNodes(childNode, RuleName.TABLE_REFERENCE);
                    if (!subTableReferenceNodes.isEmpty()) {
                        extractAndFillTableSegment(fromWhereSegment, subTableReferenceNodes, questionNodeIndexMap);
                    }
                    continue;
                }
                if (RuleName.TABLE_FACTOR.getName().equals(childNode.getClass().getSimpleName()) && fillSubquery(fromWhereSegment, childNode)) {
                    continue;
                }
                fillTable(fromWhereSegment, childNode, questionNodeIndexMap);
            }
        }
    }
    
    private boolean fillSubquery(final FromWhereSegment fromWhereSegment, final ParserRuleContext tableFactorNode) {
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(tableFactorNode, RuleName.SUBQUERY);
        if (!subqueryNode.isPresent()) {
            return false;
        }
        Optional<SubquerySegment> result = new SubqueryExtractor().extract(subqueryNode.get());
        if (result.isPresent()) {
            fromWhereSegment.getSubquerys().add(result.get());
        }
        return true;
    }
    
    private void fillTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext joinOrTableFactorNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        if (!RuleName.JOIN_TABLE.getName().endsWith(joinOrTableFactorNode.getClass().getSimpleName())) {
            Optional<TableSegment> tableSegment = tableNameExtractor.extract(joinOrTableFactorNode);
            fillTableResult(fromWhereSegment, tableSegment.get());
        }
        Optional<ParserRuleContext> joinConditionNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.JOIN_CONDITION);
        if (joinConditionNode.isPresent()) {
            ParserRuleContext tableFactorNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.TABLE_FACTOR).get();
            Optional<TableSegment> tableSegment = tableNameExtractor.extract(tableFactorNode);
            TableJoinSegment tableJoinResult = new TableJoinSegment(tableSegment.get());
            Optional<OrConditionSegment> conditionResult = buildCondition(joinConditionNode.get(), questionNodeIndexMap, fromWhereSegment.getTableAliases());
            if (conditionResult.isPresent()) {
                tableJoinResult.getJoinConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
                fromWhereSegment.getConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
            }
            fillTableResult(fromWhereSegment, tableJoinResult);
        }
    }
    
    private void fillTableResult(final FromWhereSegment fromWhereSegment, final TableSegment tableSegment) {
        String alias = tableSegment.getName();
        if (tableSegment.getAlias().isPresent()) {
            alias = tableSegment.getAlias().get();
        }
        fromWhereSegment.getTableAliases().put(alias, tableSegment.getName());
    }
    
    private Optional<OrConditionSegment> buildCondition(final ParserRuleContext node, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final Map<String, String> tableAliases) {
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode(node, RuleName.EXPR);
        if (exprNode.isPresent()) {
            return predicateSegmentExtractor.extractCondition(questionNodeIndexMap, exprNode.get());
        }
        return Optional.absent();
    }
    
    private void extractAndFillWhere(final FromWhereSegment fromWhereSegment, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return;
        }
        Optional<OrConditionSegment> conditions = buildCondition((ParserRuleContext) whereNode.get().getChild(1), questionNodeIndexMap, fromWhereSegment.getTableAliases());
        if (conditions.isPresent()) {
            fromWhereSegment.getConditions().getAndConditions().addAll(conditions.get().getAndConditions());
        }
    }
}
