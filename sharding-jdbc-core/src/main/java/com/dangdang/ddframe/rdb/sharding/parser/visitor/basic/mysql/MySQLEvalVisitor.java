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
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlEvalVisitorImpl;

import java.util.Map;

/**
 * MySQL变量中提取参数值与编号.
 * 
 * @author gaohongtao
 */
public class MySQLEvalVisitor extends MySqlEvalVisitorImpl {
    
    public static final String EVAL_VAR_INDEX = "EVAL_VAR_INDEX";
    
    @Override
    public boolean visit(final SQLVariantRefExpr x) {
        if (!"?".equals(x.getName())) {
            return false;
        }
    
        Map<String, Object> attributes = x.getAttributes();
    
        int varIndex = x.getIndex();
        
        if (varIndex == -1 || getParameters().size() <= varIndex) {
            return false;
        }
        if (attributes.containsKey(EVAL_VALUE)) {
            return false;
        }
        Object value = getParameters().get(varIndex);
        if (value == null) {
            value = EVAL_VALUE_NULL;
        }
        attributes.put(EVAL_VALUE, value);
        attributes.put(EVAL_VAR_INDEX, varIndex);
        return false;
    }
}
