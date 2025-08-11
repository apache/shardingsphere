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

package org.apache.shardingsphere.sql.parser.hive.visitor.statement.type;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowConnectorsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.hive.visitor.statement.HiveStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.hive.dal.show.HiveShowConnectorsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
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
}
