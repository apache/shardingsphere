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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.StarExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;

/**
 * Select expression extractor.
 *
 * @author duhongjun
 */
public class SelectExpressionExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<SelectExpressionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClaseNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClaseNode.isPresent()) {
            return Collections.emptyList();
        }
        Optional<ParserRuleContext> selectExprsNode = ASTUtils.findFirstChildNode(selectClaseNode.get(), RuleName.SELECT_EXPRS);
        if (!selectExprsNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<SelectExpressionSegment> result = new LinkedList<>();
        for (int i = 0; i < selectExprsNode.get().getChildCount(); i++) {
            ParseTree childNode = selectExprsNode.get().getChild(0);
            if(childNode instanceof TerminalNodeImpl) {
                continue;
            }
            String firstChildText = childNode.getText();
            if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
                int pos = firstChildText.indexOf(Symbol.DOT.getLiterals());
                String owner = "";
                if (0 < pos) {
                    owner = firstChildText.substring(0, pos);
                }
                result.add(new StarExpressionSegment(Optional.of(owner)));
            } else {
                Optional<ParserRuleContext> aliasNode = ASTUtils.findFirstChildNode((ParserRuleContext)childNode, RuleName.ALIAS);
                Optional<String> alias = null;
                if (aliasNode.isPresent()) {
                    alias = Optional.of(aliasNode.get().getText());
                } else {
                    alias = Optional.absent();
                }
                
                Optional<ColumnSegment> columnSegment = new ColumnSegmentExtractor(new HashMap<String,String>()).extract((ParserRuleContext)childNode);
                if(columnSegment.isPresent()) {
                    result.add(new PropertyExpressionSegment(columnSegment.get().getOwner().get(), columnSegment.get().getName(), columnSegment.get().getStartPosition()));
                }else {
                    Optional<ParserRuleContext> functionCall = ASTUtils.findFirstChildNode((ParserRuleContext)childNode, RuleName.FUNCTION_CALL);
                    if (functionCall.isPresent()) {
                        String name = functionCall.get().getChild(0).getText();
                        //TODO best choice using index
                        StringBuilder builder = new StringBuilder();
                        for (int j = 1; i < functionCall.get().getChildCount(); j++) {
                            builder.append(functionCall.get().getChild(j).getText());
                        }
                        result.add(new FunctionExpressionSegment(name, getParseTreeText(childNode.getChild(0)), alias));
                    } else {
                        result.add(new CommonExpressionSegment(getParseTreeText(childNode.getChild(0)), alias));
                    }
                }
            }
        }
        return result;
    }
    
    private String getParseTreeText(ParseTree node) {
        if(node.getChildCount() < 2) {
           return node.getText();
        }
      //TODO best choice using index
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            builder.append(node.getChild(i).getText());
        }
        return builder.toString();
    }
}
