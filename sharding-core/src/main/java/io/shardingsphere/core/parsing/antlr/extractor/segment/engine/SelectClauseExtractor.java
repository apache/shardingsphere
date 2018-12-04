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
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashMap;

/**
 * Select clause extractor.
 *
 * @author duhongjun
 */
public class SelectClauseExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SelectClauseSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClauseNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> selectExpressionNode = ASTUtils.findFirstChildNode(selectClauseNode.get(), RuleName.SELECT_EXPRS);
        if (!selectExpressionNode.isPresent()) {
            return Optional.absent();
        }
        SelectClauseSegment result = new SelectClauseSegment(selectExpressionNode.get().getStop().getStopIndex() + 2);
        for (int i = 0; i < selectExpressionNode.get().getChildCount(); i++) {
            ParseTree childNode = selectExpressionNode.get().getChild(i);
            if (childNode instanceof TerminalNodeImpl) {
                continue;
            }
            String firstChildText = childNode.getText();
            if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
                int position = firstChildText.indexOf(Symbol.DOT.getLiterals());
                Optional<String> owner = Optional.absent();
                if (0 < position) {
                    owner = Optional.of(SQLUtil.getExactlyValue(firstChildText.substring(0, position)));
                }
                result.getExpressions().add(new StarExpressionSegment(((ParserRuleContext) childNode).getStart().getStartIndex(), owner));
            } else {
                Optional<ParserRuleContext> aliasNode = ASTUtils.findFirstChildNode((ParserRuleContext) childNode, RuleName.ALIAS);
                Optional<String> alias = aliasNode.isPresent() ? Optional.of(SQLUtil.getExactlyValue(aliasNode.get().getText())) : Optional.<String>absent();
                Optional<ParserRuleContext> functionCall = ASTUtils.findFirstChildNode((ParserRuleContext) childNode, RuleName.FUNCTION_CALL);
                if (functionCall.isPresent()) {
                    String name = functionCall.get().getChild(0).getText();
                    int startIndex = functionCall.get().getStart().getStartIndex() + name.length();
                    result.getExpressions().add(new FunctionExpressionSegment(name, alias, startIndex, functionCall.get().getStop().getStopIndex()));
                } else {
                    if (RuleName.COLUMN_NAME.getName().equals(childNode.getClass().getSimpleName())) {
                        ParserRuleContext columnNode = (ParserRuleContext) childNode;
                        Optional<ColumnSegment> columnSegment = new ColumnSegmentExtractor(new HashMap<String, String>()).extract(columnNode);
                        PropertyExpressionSegment propertySegment = new PropertyExpressionSegment(columnSegment.get().getOwner(), columnSegment.get().getName(),
                                columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex(), alias);
                        result.getExpressions().add(propertySegment);
                    } else {
                        result.getExpressions().add(new CommonExpressionSegment(getParseTreeText(childNode.getChild(0)), alias));
                    }
                }
            }
        }
        return Optional.of(result);
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
