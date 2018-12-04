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

import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Select clause extractor.
 *
 * @author duhongjun
 */
public class SelectClauseExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SelectClauseSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClaseNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClaseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> selectExprsNode = ASTUtils.findFirstChildNode(selectClaseNode.get(), RuleName.SELECT_EXPRS);
        if (!selectExprsNode.isPresent()) {
            return Optional.absent();
        }
        SelectClauseSegment result = new SelectClauseSegment(selectExprsNode.get().getStop().getStopIndex() + 2);
        for (int i = 0; i < selectExprsNode.get().getChildCount(); i++) {
            ParseTree childNode = selectExprsNode.get().getChild(i);
            if (childNode instanceof TerminalNodeImpl) {
                continue;
            }
            String firstChildText = childNode.getText();
            if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
                int pos = firstChildText.indexOf(Symbol.DOT.getLiterals());
                Optional<String> owner = Optional.<String>absent();
                if (0 < pos) {
                    owner = Optional.of(SQLUtil.getExactlyValue(firstChildText.substring(0, pos)));
                }
                result.getExpressions().add(new StarExpressionSegment(((ParserRuleContext) childNode).getStart().getStartIndex(), owner));
            } else {
                Optional<ParserRuleContext> subqueryNode = ASTUtils.findFirstChildNode((ParserRuleContext) childNode, RuleName.SUBQUERY);
                if(subqueryNode.isPresent()) {
                    Optional<SubquerySegment> subquerySegment = new SubqueryExtractor().extract(subqueryNode.get());
                    if(subquerySegment.isPresent()) {
                        result.getExpressions().add(subquerySegment.get());
                    }
                    continue;
                }
                fillForPropertyOrFunction(result, (ParserRuleContext) childNode);
            }
        }
        return Optional.of(result);
    }
    
    private void fillForPropertyOrFunction(final SelectClauseSegment selectClauseSegment, final ParserRuleContext node) {
        Optional<ParserRuleContext> aliasNode = ASTUtils.findFirstChildNode(node, RuleName.ALIAS);
        Optional<String> alias = null;
        if (aliasNode.isPresent()) {
            alias = Optional.of(SQLUtil.getExactlyValue(aliasNode.get().getText()));
        } else {
            alias = Optional.absent();
        }
        Optional<ParserRuleContext> functionCall = ASTUtils.findFirstChildNode(node, RuleName.FUNCTION_CALL);
        if (functionCall.isPresent()) {
            String name = functionCall.get().getChild(0).getText();
            int startIndex = functionCall.get().getStart().getStartIndex() + name.length();
            selectClauseSegment.getExpressions().add(new FunctionExpressionSegment(name, alias, startIndex, functionCall.get().getStop().getStopIndex()));
        } else {
            if (RuleName.COLUMN_NAME.getName().equals(node.getClass().getSimpleName())) {
                ParserRuleContext columnNode = node;
                Optional<ColumnSegment> columnSegment = new ColumnSegmentExtractor(new HashMap<String, String>()).extract(columnNode);
                PropertyExpressionSegment propertySegment = new PropertyExpressionSegment(columnSegment.get().getOwner(), columnSegment.get().getName(),
                        columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex(), alias);
                selectClauseSegment.getExpressions().add(propertySegment);
            } else {
                selectClauseSegment.getExpressions().add(new CommonExpressionSegment(getParseTreeText(node.getChild(0)), alias));
            }
        }
    }
    
    private String getParseTreeText(final ParseTree node) {
        if (node.getChildCount() < 2) {
            return node.getText();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            builder.append(node.getChild(i).getText());
        }
        return builder.toString();
    }
}
