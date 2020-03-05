/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.relation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dal.DescribeStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dal.ShowIndexStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dcl.DenyUserStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dcl.RevokeStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.ddl.TruncateStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DenyUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;

import java.util.List;

/**
 * SQL statement context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementContextFactory {
    
    /**
     * Create SQL statement context.
     *
     * @param relationMetas relation metas
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatement SQL statement
     * @return SQL statement context
     */
    public static SQLStatementContext newInstance(final RelationMetas relationMetas, final String sql, final List<Object> parameters, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DMLStatement) {
            return getDMLStatementContext(relationMetas, sql, parameters, (DMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLStatementContext((DDLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLStatementContext((DCLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALStatementContext((DALStatement) sqlStatement);
        }
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private static SQLStatementContext getDMLStatementContext(final RelationMetas relationMetas, final String sql, final List<Object> parameters, final DMLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return new SelectStatementContext(relationMetas, sql, parameters, (SelectStatement) sqlStatement);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new UpdateStatementContext((UpdateStatement) sqlStatement);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new DeleteStatementContext((DeleteStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new InsertStatementContext(relationMetas, parameters, (InsertStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static SQLStatementContext getDDLStatementContext(final DDLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            return new CreateTableStatementContext((CreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return new AlterTableStatementContext((AlterTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropTableStatement) {
            return new DropTableStatementContext((DropTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateIndexStatement) {
            return new CreateIndexStatementContext((CreateIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterIndexStatement) {
            return new AlterIndexStatementContext((AlterIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new DropIndexStatementContext((DropIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof TruncateStatement) {
            return new TruncateStatementContext((TruncateStatement) sqlStatement);
        }
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private static SQLStatementContext getDCLStatementContext(final DCLStatement sqlStatement) {
        if (sqlStatement instanceof GrantStatement) {
            return new GrantStatementContext((GrantStatement) sqlStatement);
        }
        if (sqlStatement instanceof RevokeStatement) {
            return new RevokeStatementContext((RevokeStatement) sqlStatement);
        }
        if (sqlStatement instanceof DenyUserStatement) {
            return new DenyUserStatementContext((DenyUserStatement) sqlStatement);
        }
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private static SQLStatementContext getDALStatementContext(final DALStatement sqlStatement) {
        if (sqlStatement instanceof DescribeStatement) {
            return new DescribeStatementContext((DescribeStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowCreateTableStatement) {
            return new ShowCreateTableStatementContext((ShowCreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowColumnsStatement) {
            return new ShowColumnsStatementContext((ShowColumnsStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowIndexStatement) {
            return new ShowIndexStatementContext((ShowIndexStatement) sqlStatement);
        }
        return new CommonSQLStatementContext(sqlStatement);
    }
}
