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
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;

/**
 * Expression extractor.
 *
 * @author duhongjun
 */
public final class ExpressionExtractor implements OptionalSQLSegmentExtractor {
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends ExpressionSegment> extract(final ParserRuleContext expressionNode) {
        String firstChildText = expressionNode.getText();
        if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
            return Optional.of(getStarExpressionSegment(expressionNode, firstChildText));
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.SUBQUERY);
        return subqueryNode.isPresent() ? subqueryExtractor.extract(subqueryNode.get()) : Optional.of(fillForPropertyOrFunction(expressionNode));
    }
    
    private ExpressionSegment getStarExpressionSegment(final ParserRuleContext expressionNode, final String text) {
        StarExpressionSegment result = new StarExpressionSegment(expressionNode.getStart().getStartIndex());
        if (text.contains(Symbol.DOT.getLiterals())) {
            result.setOwner(SQLUtil.getExactlyValue(text.substring(0, text.indexOf(Symbol.DOT.getLiterals()))));
        }
        return result;
    }
    
    private ExpressionSegment fillForPropertyOrFunction(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> functionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (functionNode.isPresent()) {
            return getFunctionExpressionSegment(expressionNode, functionNode.get());
        }
        if (RuleName.COLUMN_NAME.getName().equals(expressionNode.getChild(0).getClass().getSimpleName())) {
            return getPropertyExpressionSegment(expressionNode);
        }
        CommonExpressionSegment result = new CommonExpressionSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex());
        Optional<String> alias = getAlias(expressionNode);
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
    
    private ExpressionSegment getFunctionExpressionSegment(final ParserRuleContext expressionNode, final ParserRuleContext functionNode) {
        String functionName = functionNode.getChild(0).getText();
        int startIndex = ((TerminalNode) functionNode.getChild(1)).getSymbol().getStartIndex();
        boolean hasDistinct = hasDistinct(expressionNode);
        int distinctColumnNameStartPosition = hasDistinct ? calculateDistinctColumnNamePosition(functionNode) : -1;
        FunctionExpressionSegment result = new FunctionExpressionSegment(functionName, 
                functionNode.getStart().getStartIndex(), startIndex, functionNode.getStop().getStopIndex(), hasDistinct, distinctColumnNameStartPosition);
        Optional<String> alias = getAlias(expressionNode);
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
    
    private boolean hasDistinct(final ParserRuleContext expressionNode) {
        return ExtractorUtils.findFirstChildNode(expressionNode, RuleName.DISTINCT).isPresent();
    }
    
    private int calculateDistinctColumnNamePosition(final ParserRuleContext functionNode) {
        ParseTree distinctItemNode = functionNode.getChild(3);
        if (distinctItemNode instanceof TerminalNode) {
            return ((TerminalNode) distinctItemNode).getSymbol().getStartIndex();
        }
        if (distinctItemNode instanceof ParserRuleContext) {
            return ((ParserRuleContext) distinctItemNode).getStart().getStartIndex();
        }
        return -1;
    }
    
    private ExpressionSegment getPropertyExpressionSegment(final ParserRuleContext expressionNode) {
        ParserRuleContext columnNode = (ParserRuleContext) expressionNode.getChild(0);
        Optional<ColumnSegment> columnSegment = new ColumnSegmentExtractor(new HashMap<String, String>()).extract(columnNode);
        Preconditions.checkState(columnSegment.isPresent());
        PropertyExpressionSegment result = new PropertyExpressionSegment(
                columnSegment.get().getOwner(), columnSegment.get().getName(), columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex());
        Optional<String> alias = getAlias(expressionNode);
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
    
    private Optional<String> getAlias(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.ALIAS);
        return aliasNode.isPresent() ? Optional.of(SQLUtil.getExactlyValue(aliasNode.get().getText())) : Optional.<String>absent();
    }
}
