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
package com.alibaba.druid.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2OutputVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleOutputVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGOutputVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;

import java.util.ArrayList;
import java.util.List;

public final class SQLUtils {
    
    public static String toSQLString(final SQLObject sqlObject, final String dbType) {
        if (JdbcConstants.MYSQL.equals(dbType) || JdbcConstants.MARIADB.equals(dbType) || JdbcConstants.H2.equals(dbType)) {
            return toMySqlString(sqlObject);
        }
        if (JdbcConstants.ORACLE.equals(dbType)) {
            return toOracleString(sqlObject);
        }
        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            return toPGString(sqlObject);
        }
        if (JdbcConstants.DB2.equals(dbType)) {
            return toDB2String(sqlObject);
        }
        return toSQLServerString(sqlObject);
    }
    
    public static String toSQLString(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new SQLASTOutputVisitor(out));
        return out.toString();
    }
    
    public static String toMySqlString(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new MySqlOutputVisitor(out));
        return out.toString();
    }
    
    public static String toOracleString(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new OracleOutputVisitor(out));
        return out.toString();
    }
    
    public static String toPGString(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new PGOutputVisitor(out));
        return out.toString();
    }
    
    public static String toDB2String(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new DB2OutputVisitor(out));
        return out.toString();
    }
    
    public static String toSQLServerString(final SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new SQLServerOutputVisitor(out));
        return out.toString();
    }
    
    public static List<SQLExpr> split(final SQLBinaryOpExpr x) {
        List<SQLExpr> groupList = new ArrayList<>();
        groupList.add(x.getRight());
        SQLExpr left = x.getLeft();
        while (true) {
            if (left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).getOperator() == x.getOperator()) {
                SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
                groupList.add(binaryLeft.getRight());
                left = binaryLeft.getLeft();
            } else {
                groupList.add(left);
                break;
            }
        }
        return groupList;
    }
}
