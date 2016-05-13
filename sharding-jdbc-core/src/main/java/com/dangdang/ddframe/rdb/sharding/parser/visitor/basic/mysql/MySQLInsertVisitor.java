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

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.google.common.base.Optional;

/**
 * MySQL的INSERT语句访问器.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public class MySQLInsertVisitor extends AbstractMySQLVisitor {
    
    @Override
    public boolean visit(final MySqlInsertStatement x) {
        getParseContext().setCurrentTable(x.getTableName().toString(), Optional.fromNullable(x.getAlias()));
        if (null == x.getValues()) {
            return super.visit(x);
        }
        for (int i = 0; i < x.getColumns().size(); i++) {
            getParseContext().addCondition(x.getColumns().get(i).toString(), x.getTableName().toString(), BinaryOperator.EQUAL, x.getValues().getValues().get(i), getDatabaseType(), getParameters());
        }
        return super.visit(x);
    }
}
