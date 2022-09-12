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

package org.apache.shardingsphere.traffic.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.AlterTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.CreateTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.DropTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.LabelDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.LoadBalancerDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.ShowTrafficRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.TrafficDistSQLStatementParser.TrafficRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.DropTrafficRuleStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for traffic dist SQL.
 */
public final class TrafficDistSQLStatementVisitor extends TrafficDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateTrafficRule(final CreateTrafficRuleContext ctx) {
        return new CreateTrafficRuleStatement(ctx.trafficRuleDefinition().stream().map(each -> (TrafficRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterTrafficRule(final AlterTrafficRuleContext ctx) {
        return new AlterTrafficRuleStatement(ctx.trafficRuleDefinition().stream().map(each -> (TrafficRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitTrafficRuleDefinition(final TrafficRuleDefinitionContext ctx) {
        AlgorithmSegment loadBalancerSegment = null;
        if (null != ctx.loadBalancerDefinition()) {
            loadBalancerSegment = (AlgorithmSegment) visit(ctx.loadBalancerDefinition().algorithmDefinition());
        }
        return new TrafficRuleSegment(getIdentifierValue(ctx.ruleName()), buildLabels(ctx.labelDefinition()),
                (AlgorithmSegment) visit(ctx.trafficAlgorithmDefinition().algorithmDefinition()), loadBalancerSegment);
    }
    
    private Collection<String> buildLabels(final LabelDefinitionContext labelDefinition) {
        return null == labelDefinition ? Collections.emptyList() : labelDefinition.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitLoadBalancerDefinition(final LoadBalancerDefinitionContext ctx) {
        return null == ctx ? null : visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), buildProperties(ctx.propertiesDefinition()));
    }
    
    private Properties buildProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTrafficRule(final DropTrafficRuleContext ctx) {
        Collection<String> ruleNames = null == ctx.ruleName() ? null : ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toSet());
        return new DropTrafficRuleStatement(null != ctx.ifExists(), ruleNames);
    }
    
    @Override
    public ASTNode visitShowTrafficRules(final ShowTrafficRulesContext ctx) {
        return new ShowTrafficRulesStatement(null == ctx.ruleName() ? null : getIdentifierValue(ctx.ruleName()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
