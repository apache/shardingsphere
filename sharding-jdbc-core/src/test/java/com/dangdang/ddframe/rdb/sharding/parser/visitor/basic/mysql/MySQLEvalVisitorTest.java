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

package com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MySQLEvalVisitorTest {
    
    @Test
    public void testVisit() throws Exception {
        SQLVariantRefExpr expr = new SQLVariantRefExpr("?");
        expr.setIndex(1);
        MySQLEvalVisitor visitor = new MySQLEvalVisitor();
        visitor.setParameters(Lists.<Object>newArrayList(1, 2));
        expr.accept(visitor);
        assertThat((Integer) SQLEvalVisitorUtils.getValue(expr), is(2));
        assertThat((Integer) expr.getAttribute(MySQLEvalVisitor.EVAL_VAR_INDEX), is(1));
    }
    
    @Test
    public void testVisitErrorIndex() throws Exception {
        SQLVariantRefExpr expr = new SQLVariantRefExpr("?");
        expr.setIndex(2);
        MySQLEvalVisitor visitor = new MySQLEvalVisitor();
        visitor.setParameters(Lists.<Object>newArrayList(1, 2));
        expr.accept(visitor);
        assertThat(SQLEvalVisitorUtils.getValue(expr), nullValue());
    }
    
    @Test
    public void testVisitNotOverride() throws Exception {
        SQLVariantRefExpr expr = new SQLVariantRefExpr("?");
        expr.setIndex(1);
        expr.getAttributes().put(MySQLEvalVisitor.EVAL_VALUE, "test");
        MySQLEvalVisitor visitor = new MySQLEvalVisitor();
        visitor.setParameters(Lists.<Object>newArrayList(1, 2));
        expr.accept(visitor);
        assertThat((String) SQLEvalVisitorUtils.getValue(expr), is("test"));
    }
    
    @Test
    public void testVisitWrongName() throws Exception {
        SQLVariantRefExpr expr = new SQLVariantRefExpr("");
        expr.setIndex(1);
        MySQLEvalVisitor visitor = new MySQLEvalVisitor();
        visitor.setParameters(Lists.<Object>newArrayList(1, 2));
        expr.accept(visitor);
        assertThat(SQLEvalVisitorUtils.getValue(expr), nullValue());
    }
    
    @Test
    public void testVisitNullValue() throws Exception {
        SQLVariantRefExpr expr = new SQLVariantRefExpr("?");
        expr.setIndex(1);
        MySQLEvalVisitor visitor = new MySQLEvalVisitor();
        visitor.setParameters(Lists.<Object>newArrayList(1, null));
        expr.accept(visitor);
        assertThat(SQLEvalVisitorUtils.getValue(expr), is(MySQLEvalVisitor.EVAL_VALUE_NULL));
    }
}
