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

package org.apache.shardingsphere.sql.parser.engine.hive.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DescribeConnectorContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DescribeDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DescribeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowCompactionsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowConfContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowConnectorsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowFromContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowFunctionsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowGrantedRolesAndPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowLocksContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowMaterializedViewsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowPartitionsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowTablesExtendedContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowTblpropertiesContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowTransactionsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowViewsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.engine.hive.visitor.statement.HiveStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.HiveDescribeStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowCompactionsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowConfStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowConnectorsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowFunctionsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowLocksStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowMaterializedViewsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowPartitionsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowTablesExtendedStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowTblpropertiesStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowTransactionsStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowViewsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.privilege.MySQLShowGrantsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;

/**
 * DAL statement visitor for Hive.
 */
public final class HiveDALStatementVisitor extends HiveStatementVisitor implements DALStatementVisitor {
    
    public HiveDALStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        String database = null == ctx.DEFAULT() ? new IdentifierValue(ctx.identifier().getText()).getValue() : "default";
        return new MySQLUseStatement(getDatabaseType(), database);
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        ShowFilterSegment filter = null;
        if (null != ctx.showLike()) {
            filter = new ShowFilterSegment(ctx.showLike().getStart().getStartIndex(), ctx.showLike().getStop().getStopIndex());
            filter.setLike((ShowLikeSegment) visit(ctx.showLike()));
        }
        MySQLShowDatabasesStatement result = new MySQLShowDatabasesStatement(getDatabaseType(), filter);
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
    
    @Override
    public ASTNode visitShowConnectors(final ShowConnectorsContext ctx) {
        return new HiveShowConnectorsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        FromDatabaseSegment fromDatabase = null;
        if (null != ctx.databaseName()) {
            DatabaseSegment databaseSegment = (DatabaseSegment) visit(ctx.databaseName());
            fromDatabase = new FromDatabaseSegment(ctx.databaseName().getStart().getStartIndex(), databaseSegment);
        }
        ShowFilterSegment filter = null;
        if (null != ctx.stringLiterals()) {
            StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
            ShowLikeSegment likeSegment = new ShowLikeSegment(ctx.stringLiterals().getStart().getStartIndex(), ctx.stringLiterals().getStop().getStopIndex(), literalValue.getValue());
            filter = new ShowFilterSegment(ctx.stringLiterals().getStart().getStartIndex(), ctx.stringLiterals().getStop().getStopIndex());
            filter.setLike(likeSegment);
        }
        MySQLShowTablesStatement result = new MySQLShowTablesStatement(getDatabaseType(), fromDatabase, filter, false);
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowViews(final ShowViewsContext ctx) {
        return new HiveShowViewsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowMaterializedViews(final ShowMaterializedViewsContext ctx) {
        return new HiveShowMaterializedViewsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowPartitions(final ShowPartitionsContext ctx) {
        return new HiveShowPartitionsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowTablesExtended(final ShowTablesExtendedContext ctx) {
        return new HiveShowTablesExtendedStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowTblproperties(final ShowTblpropertiesContext ctx) {
        return new HiveShowTblpropertiesStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        return new MySQLShowCreateTableStatement(getDatabaseType(), (SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        FromDatabaseSegment fromDatabase = null;
        if (null != ctx.showFrom()) {
            fromDatabase = createFromDatabaseSegment(ctx.showFrom());
        }
        return new MySQLShowIndexStatement(getDatabaseType(), (SimpleTableSegment) visit(ctx.tableName()), fromDatabase);
    }
    
    private FromDatabaseSegment createFromDatabaseSegment(final ShowFromContext showFromContext) {
        ASTNode showFromNode = visit(showFromContext);
        if (showFromNode instanceof DatabaseSegment) {
            return new FromDatabaseSegment(showFromContext.getStart().getStartIndex(), (DatabaseSegment) showFromNode);
        }
        return null;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        SimpleTableSegment table = null;
        if (null != ctx.tableName()) {
            table = (SimpleTableSegment) visit(ctx.tableName());
        }
        FromDatabaseSegment fromDatabase = null;
        if (null != ctx.showFrom()) {
            fromDatabase = createFromDatabaseSegment(ctx.showFrom());
        }
        ShowFilterSegment filter = null;
        if (null != ctx.showLike()) {
            filter = new ShowFilterSegment(ctx.showLike().getStart().getStartIndex(), ctx.showLike().getStop().getStopIndex());
            filter.setLike((ShowLikeSegment) visit(ctx.showLike()));
        }
        return new MySQLShowColumnsStatement(getDatabaseType(), table, fromDatabase, filter);
    }
    
    @Override
    public ASTNode visitShowFunctions(final ShowFunctionsContext ctx) {
        return new HiveShowFunctionsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowGrantedRolesAndPrivileges(final ShowGrantedRolesAndPrivilegesContext ctx) {
        return new MySQLShowGrantsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowLocks(final ShowLocksContext ctx) {
        return new HiveShowLocksStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowConf(final ShowConfContext ctx) {
        return new HiveShowConfStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowTransactions(final ShowTransactionsContext ctx) {
        return new HiveShowTransactionsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitShowCompactions(final ShowCompactionsContext ctx) {
        return new HiveShowCompactionsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDescribeDatabase(final DescribeDatabaseContext ctx) {
        return new HiveDescribeStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDescribeConnector(final DescribeConnectorContext ctx) {
        return new HiveDescribeStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDescribeTable(final DescribeTableContext ctx) {
        SimpleTableSegment table = (SimpleTableSegment) visit(ctx.tableName());
        ColumnSegment columnWildcard = null;
        if (null != ctx.columnClause()) {
            columnWildcard = (ColumnSegment) visit(ctx.columnClause().columnName());
        }
        return new MySQLDescribeStatement(getDatabaseType(), table, columnWildcard);
    }
}
