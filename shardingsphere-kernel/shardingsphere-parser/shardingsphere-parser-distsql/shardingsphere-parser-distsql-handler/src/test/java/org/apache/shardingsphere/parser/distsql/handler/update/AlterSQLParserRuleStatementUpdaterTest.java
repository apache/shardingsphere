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

package org.apache.shardingsphere.parser.distsql.handler.update;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AlterSQLParserRuleStatementUpdaterTest {
    
    @Test
    public void assertExecute() throws SQLException {
        AlterSQLParserRuleStatementUpdater updater = new AlterSQLParserRuleStatementUpdater();
        AlterSQLParserRuleStatement sqlStatement = new AlterSQLParserRuleStatement(true, new CacheOptionSegment(64, 512L), new CacheOptionSegment(1000, 1000L));
        ShardingSphereMetaData metaData = createMetaData();
        updater.executeUpdate(metaData, sqlStatement);
        SQLParserRule actual = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        assertTrue(actual.isSqlCommentParseEnabled());
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(1000));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(1000L));
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(64));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(512L));
    }
    
    private ShardingSphereMetaData createMetaData() {
        SQLParserRule rule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(rule)));
        return new ShardingSphereMetaData(Collections.emptyMap(), ruleMetaData, new ConfigurationProperties(new Properties()));
    }
}
