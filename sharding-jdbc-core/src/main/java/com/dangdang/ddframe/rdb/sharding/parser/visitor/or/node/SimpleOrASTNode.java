/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.visitor.or.node;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.wall.WallVisitorUtils;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.or.OrVisitor;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;

/**
 * 只包含OR的节点.
 * 
 * @author gaohongtao
 */
@AllArgsConstructor
public class SimpleOrASTNode extends AbstractOrASTNode {
    
    private SQLBinaryOpExpr canSplitExpr;
    
    private final OrVisitor orVisitor;
    
    @Override
    public void createOrASTAsRootNode() {
        if (SQLBinaryOperator.BooleanOr == canSplitExpr.getOperator()) {
            parseExprIfNotFalse(canSplitExpr.getRight());
            if (canSplitExpr.getLeft() instanceof SQLBinaryOpExpr) {
                canSplitExpr = (SQLBinaryOpExpr) canSplitExpr.getLeft();
                createOrASTAsRootNode();
            } else {
                finishParseThisNode(canSplitExpr.getLeft());
            }
        } else {
            finishParseThisNode(canSplitExpr);
        }
    }
    
    private void finishParseThisNode(final SQLExpr expr) {
        parseExprIfNotFalse(expr);
        mergeSubConditions();
    }
    
    private void parseExprIfNotFalse(final SQLExpr expr) {
        if (Boolean.FALSE.equals(WallVisitorUtils.getValue(expr))) {
            return;
        }
        Optional<AbstractOrASTNode> subNode = orVisitor.visitHandle(expr);
        if (subNode.isPresent()) {
            addSubNode(subNode.get());
        } else {
            addNestedConditions(orVisitor.getParseContext().getCurrentConditionContext());
        }
    }
}
