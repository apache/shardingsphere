/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */
package com.alibaba.druid.sql.visitor;

import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.visitor.functions.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLEvalVisitorImpl extends SQLASTVisitorAdapter implements SQLEvalVisitor {
    
    private final Map<String, Function> functions = new HashMap<>();
    
    private List<Object> parameters = new ArrayList<>();
    
    public SQLEvalVisitorImpl(){
        this(new ArrayList<>(1));
    }
    
    public SQLEvalVisitorImpl(final List<Object> parameters){
        this.parameters = parameters;
    }
    
    @Override
    public List<Object> getParameters() {
        return parameters;
    }
    
    @Override
    public void setParameters(final List<Object> parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public boolean visit(final SQLCharExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLVariantRefExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public boolean visit(final SQLBinaryOpExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public boolean visit(final SQLIntegerExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLNumberExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLHexExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLCaseExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public boolean visit(final SQLInListExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public boolean visit(final SQLNullExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLMethodInvokeExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public boolean visit(final SQLQueryExpr x) {
        return SQLEvalVisitorUtils.visit(this, x);
    }
    
    @Override
    public Function getFunction(final String funcName) {
        return functions.get(funcName);
    }
    
    @Override
    public void registerFunction(final String funcName, final Function function) {
        functions.put(funcName, function);
    }
    
    public boolean visit(final SQLIdentifierExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
    
    @Override
    public boolean visit(final SQLBooleanExpr x) {
        x.getAttributes().put(EVAL_VALUE, x.isValue());
        return false;
    }
    
    @Override
    public boolean visit(final SQLBinaryExpr x) {
        return SQLEvalVisitorUtils.visit(x);
    }
}
