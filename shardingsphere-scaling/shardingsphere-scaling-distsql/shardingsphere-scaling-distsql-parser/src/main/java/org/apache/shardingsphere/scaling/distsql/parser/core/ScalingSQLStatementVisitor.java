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

package org.apache.shardingsphere.scaling.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CheckScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DropScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ResetScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingJobListContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingJobStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StartScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingSourceWritingContext;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingJobStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropScalingJobStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ResetScalingJobStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobListStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StartScalingJobStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingJobStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL statement visitor for scaling.
 */
public final class ScalingSQLStatementVisitor extends ScalingStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitShowScalingJobList(final ShowScalingJobListContext ctx) {
        return new ShowScalingJobListStatement();
    }
    
    @Override
    public ASTNode visitShowScalingJobStatus(final ShowScalingJobStatusContext ctx) {
        return new ShowScalingJobStatusStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStartScalingJob(final StartScalingJobContext ctx) {
        return new StartScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStopScalingJob(final StopScalingJobContext ctx) {
        return new StopScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitDropScalingJob(final DropScalingJobContext ctx) {
        return new DropScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitResetScalingJob(final ResetScalingJobContext ctx) {
        return new ResetScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckScalingJob(final CheckScalingJobContext ctx) {
        return new CheckScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitShowScalingCheckAlgorithms(final ShowScalingCheckAlgorithmsContext ctx) {
        return new ShowScalingCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopScalingSourceWriting(final StopScalingSourceWritingContext ctx) {
        return new StopScalingSourceWritingStatement(Long.parseLong(ctx.jobId().getText()));
    }
}
