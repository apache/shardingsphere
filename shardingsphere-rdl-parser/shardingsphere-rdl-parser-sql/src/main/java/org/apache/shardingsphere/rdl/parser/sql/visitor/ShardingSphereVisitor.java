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

package org.apache.shardingsphere.rdl.parser.sql.visitor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.rdl.parser.autogen.ShardingSphereStatementBaseVisitor;
import org.apache.shardingsphere.rdl.parser.autogen.ShardingSphereStatementParser.CreateDatasourceContext;
import org.apache.shardingsphere.rdl.parser.autogen.ShardingSphereStatementParser.DatasourceContext;
import org.apache.shardingsphere.rdl.parser.autogen.ShardingSphereStatementParser.DatasourceValueContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.DataSourceConnectionSegment;
import org.apache.shardingsphere.sql.parser.api.ASTNode;

import java.util.Collection;
import java.util.LinkedList;

/**
 * ShardingSphere visitor.
 */
@Getter(AccessLevel.PROTECTED)
public final class ShardingSphereVisitor extends ShardingSphereStatementBaseVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateDatasource(final CreateDatasourceContext ctx) {
        Collection<DataSourceConnectionSegment> connectionInfos = new LinkedList<>();
        for (DatasourceContext each : ctx.datasource()) {
            connectionInfos.add((DataSourceConnectionSegment) visit(each));
        }
        return new CreateDataSourcesStatement(connectionInfos);
    }
    
    @Override
    public ASTNode visitDatasource(final DatasourceContext ctx) {
        DataSourceConnectionSegment result = (DataSourceConnectionSegment) visitDatasourceValue(ctx.datasourceValue());
        result.setName(ctx.key().getText());
        return result;
    }
    
    @Override
    public ASTNode visitDatasourceValue(final DatasourceValueContext ctx) {
        DataSourceConnectionSegment result = new DataSourceConnectionSegment();
        result.setHostName(ctx.hostName().getText());
        result.setPort(ctx.port().getText());
        result.setDb(ctx.dbName().getText());
        result.setUser(null == ctx.user() ? "" : ctx.user().getText());
        result.setPassword(null == ctx.password() ? "" : ctx.password().getText());
        return result;
    }
}
