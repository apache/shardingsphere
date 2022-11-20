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

package org.apache.shardingsphere.dbdiscovery.distsql.parser;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.facade.DatabaseDiscoveryDistSQLStatementParserFacade;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.core.featured.FeaturedDistSQLStatementParserFacadeFactory;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DatabaseDiscoveryDistSQLTest {
    
    @Test
    public void assertCreateDatabaseDiscoveryRule() {
        String sql = "CREATE DB_DISCOVERY RULE db_discovery_group_0 ("
                + "STORAGE_UNITS(ds_0, ds_1), TYPE(NAME='mgr',PROPERTIES('group-name'='92504d5b')),"
                + "HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?')))";
        CreateDatabaseDiscoveryRuleStatement distSQLStatement = (CreateDatabaseDiscoveryRuleStatement) getDistSQLStatement(sql);
        assertThat(distSQLStatement.getRules().size(), is(1));
        assertDiscoverySegment((DatabaseDiscoveryDefinitionSegment) distSQLStatement.getRules().iterator().next());
    }
    
    @Test
    public void assertAlterDatabaseDiscoveryRule() {
        String sql = "ALTER DB_DISCOVERY RULE db_discovery_group_0 ("
                + "STORAGE_UNITS(ds_0, ds_1), TYPE(NAME='mgr',PROPERTIES('group-name'='92504d5b')),"
                + "HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?')))";
        AlterDatabaseDiscoveryRuleStatement distSQLStatement = (AlterDatabaseDiscoveryRuleStatement) getDistSQLStatement(sql);
        assertThat(distSQLStatement.getRules().size(), is(1));
        assertDiscoverySegment((DatabaseDiscoveryDefinitionSegment) distSQLStatement.getRules().iterator().next());
    }
    
    private void assertDiscoverySegment(final DatabaseDiscoveryDefinitionSegment discoverySegment) {
        assertThat(discoverySegment.getName(), is("db_discovery_group_0"));
        assertThat(discoverySegment.getDataSources(), is(Arrays.asList("ds_0", "ds_1")));
        Properties props = new Properties();
        props.setProperty("group-name", "92504d5b");
        assertThat(discoverySegment.getDiscoveryType().getName(), equalTo("mgr"));
        assertThat(discoverySegment.getDiscoveryType().getProps(), equalTo(props));
        Properties heartbeatProps = new Properties();
        heartbeatProps.setProperty("keep-alive-cron", "0/5 * * * * ?");
        assertThat(discoverySegment.getDiscoveryHeartbeat(), is(heartbeatProps));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getDistSQLStatement(final String sql) {
        DatabaseDiscoveryDistSQLStatementParserFacade facade = new DatabaseDiscoveryDistSQLStatementParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = FeaturedDistSQLStatementParserFacadeFactory.getInstance(facade.getType()).getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
