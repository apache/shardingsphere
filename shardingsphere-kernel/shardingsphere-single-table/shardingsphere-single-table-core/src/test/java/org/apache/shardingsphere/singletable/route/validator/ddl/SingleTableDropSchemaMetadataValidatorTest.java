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

package org.apache.shardingsphere.singletable.route.validator.ddl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.fixtrue.FixtureRule;
import org.apache.shardingsphere.singletable.route.validator.SingleTableMetadataValidator;
import org.apache.shardingsphere.singletable.route.validator.SingleTableMetadataValidatorFactory;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class SingleTableDropSchemaMetadataValidatorTest {
    
    @Test
    public void assertGetInstance() {
        FixtureRule rule = mock(FixtureRule.class);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                createSingleDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        DropSchemaStatement sqlStatement = new PostgreSQLDropSchemaStatement();
        sqlStatement.getSchemaNames().add(new IdentifierValue("t_order_item"));
        sqlStatement.setParameterCount(1);
        SQLStatementContext<DropSchemaStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        Optional<SingleTableMetadataValidator> result = SingleTableMetadataValidatorFactory.newInstance(sqlStatementContext.getSqlStatement());
        String msg = "";
        try {
            result.ifPresent(optional -> optional.validate(singleTableRule, sqlStatementContext, database));
        } catch (ShardingSphereException ex) {
            msg = ex.getMessage();
        }
        assertTrue(msg, result.isPresent());
    }
    
    private Map<String, DataSource> createSingleDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
