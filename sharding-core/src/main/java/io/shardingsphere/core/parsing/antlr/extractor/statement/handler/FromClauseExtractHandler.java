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

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableJoinExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Extract table result.
 *
 * @author duhongjun
 */
public final class FromClauseExtractHandler implements ASTExtractHandler<Collection<TableExtractResult>> {
    
    private final TableNameExtractHandler tableNameExtractHandler = new TableNameExtractHandler();
    
    @Override
    public Collection<TableExtractResult> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> fromNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ASTUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<TableExtractResult> result = new LinkedList<>();
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
                result.add(extractResult.get());
                continue;
            }
            Optional<ParserRuleContext> joinConditionNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.JOIN_CONDITION);
            if (joinConditionNode.isPresent()) {
                Optional<ParserRuleContext> exprNode = ASTUtils.findFirstChildNode(joinTableNode.get(), RuleName.EXPR);
                if (exprNode.isPresent()) {
                    TableJoinExtractResult tableJoinResult = new TableJoinExtractResult(extractResult.get());
                    //TODO extract condition
                    result.add(tableJoinResult);
                    continue;
                }
            }
            result.add(extractResult.get());
        }
        return result;
    }
}
