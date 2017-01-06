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

package com.dangdang.ddframe.rdb.sharding.parser.visitor.or;

import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.wall.WallVisitorUtils;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.SQLVisitor;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql.AbstractMySQLVisitor;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.or.node.AbstractOrASTNode;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.or.node.CompositeOrASTNode;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.or.node.SimpleOrASTNode;
import com.google.common.base.Optional;

/**
 * 逻辑OR条件访问器.
 * 
 * @author gaohongtao
 */
public class OrVisitor extends AbstractMySQLVisitor {
    
    private AbstractOrASTNode orASTNode;
    
    public OrVisitor(final SQLASTOutputVisitor dependencyVisitor) {
        getParameters().addAll(dependencyVisitor.getParameters());
        SQLVisitor visitor = (SQLVisitor) dependencyVisitor;
        if (null != visitor.getParseContext().getCurrentTable()) {
            getParseContext().setCurrentTable(visitor.getParseContext().getCurrentTable().getName(), Optional.<String>absent());
        }
        getParseContext().getParsedResult().getRouteContext().getTables().addAll(visitor.getParseContext().getParsedResult().getRouteContext().getTables());
        getParseContext().setShardingRule(visitor.getParseContext().getShardingRule());
    }
    
    /**
     * 进行OR表达式的访问.
     *
     * @param sqlObject SQL对象
     * @return OR访问节点
     */
    public Optional<AbstractOrASTNode> visitHandle(final SQLObject sqlObject) {
        reset();
        sqlObject.accept(this);
        postVisitHandle();
        return Optional.fromNullable(orASTNode);
    }
    
    private void reset() {
        orASTNode = null;
        getParseContext().getCurrentConditionContext().clear();
        getParseContext().setHasOrCondition(false);
    }
    
    private void postVisitHandle() {
        if (null == orASTNode) {
            return;
        }
        if (!getParseContext().getCurrentConditionContext().isEmpty()) {
            CompositeOrASTNode existingOutConditionOrASTNode = new CompositeOrASTNode();
            existingOutConditionOrASTNode.addSubNode(orASTNode);
            existingOutConditionOrASTNode.addOutConditions(getParseContext().getCurrentConditionContext());
            orASTNode = existingOutConditionOrASTNode;
        }
        orASTNode.createOrASTAsRootNode();
    }
    
    /**
     * 逻辑OR访问器, 每次只解析一层OR条件.
     * 
     * @param x 二元表达式
     * @return false 停止访问AST
     */
    @Override
    public boolean visit(final SQLBinaryOpExpr x) {
        if (!SQLBinaryOperator.BooleanOr.equals(x.getOperator())) {
            return super.visit(x);
        }
        if (Boolean.TRUE.equals(WallVisitorUtils.getValue(x))) {
            return false;
        }
        if (orASTNode == null) {
            orASTNode = new SimpleOrASTNode(x, new OrVisitor(this));
        } else {
            CompositeOrASTNode existingOutConditionOrASTNode = new CompositeOrASTNode();
            existingOutConditionOrASTNode.addSubNode(orASTNode);
            existingOutConditionOrASTNode.addSubNode(new SimpleOrASTNode(x, new OrVisitor(this)));
            orASTNode = existingOutConditionOrASTNode;
        }
        return false;
    }
}
