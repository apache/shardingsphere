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

package org.apache.shardingsphere.encrypt.checker.cryptographic;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsertSelectColumnsEncryptorCheckerTest {
    
    @Test
    void assertInsertSelectIsSame() {
        String databaseName = "foo_db";
        IdentifierValue databaseValue = new IdentifierValue(databaseName);
        IdentifierValue schemaValue = new IdentifierValue("schema");
        ColumnSegment insertColumn1 = getInsertColumnSegment(databaseValue, schemaValue, "table1", "pwd");
        ColumnSegment insertColumn2 = getInsertColumnSegment(databaseValue, schemaValue, "table1", "card");
        ColumnProjection projection1 = getSelectProjection("pwd", databaseValue, schemaValue);
        ColumnProjection projection2 = getSelectProjection("card", databaseValue, schemaValue);
        EncryptRule encryptRule = new EncryptRule(databaseName, createEncryptRuleConfiguration());
        boolean result = InsertSelectColumnsEncryptorChecker.isSame(Arrays.asList(insertColumn1, insertColumn2), Arrays.asList(projection1, projection2), encryptRule);
        assertTrue(result);
    }
    
    private ColumnProjection getSelectProjection(final String pwd, final IdentifierValue databaseValue, final IdentifierValue schemaValue) {
        TableSegmentBoundInfo tableSegmentBoundInfo = new TableSegmentBoundInfo(databaseValue, schemaValue);
        return new ColumnProjection(new IdentifierValue("table2"), new IdentifierValue(pwd), null, null, null, null,
                new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue("table2"), new IdentifierValue(pwd), TableSourceType.TEMPORARY_TABLE));
    }
    
    private ColumnSegment getInsertColumnSegment(final IdentifierValue databaseValue, final IdentifierValue schemaValue, final String tableName, final String columnName) {
        ColumnSegment result = mock(ColumnSegment.class);
        TableSegmentBoundInfo tableSegmentBoundInfo = new TableSegmentBoundInfo(databaseValue, schemaValue);
        when(result.getColumnBoundInfo())
                .thenReturn(new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue(tableName), new IdentifierValue(columnName), TableSourceType.TEMPORARY_TABLE));
        return result;
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        return new EncryptRuleConfiguration(Arrays.asList(getEncryptTableRuleConfiguration("table1"), getEncryptTableRuleConfiguration("table2")),
                getEncryptors(new AlgorithmConfiguration("CORE.FIXTURE", new Properties()),
                        new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties()), new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties())));
    }
    
    private EncryptTableRuleConfiguration getEncryptTableRuleConfiguration(final String tableName) {
        EncryptColumnRuleConfiguration pwdColumnConfig = createEncryptColumnRuleConfiguration("pwd", "standard_encryptor");
        EncryptColumnRuleConfiguration cardColumnConfig = new EncryptColumnRuleConfiguration("card", new EncryptColumnItemRuleConfiguration("card_cipher", "standard_encryptor"));
        return new EncryptTableRuleConfiguration(tableName, Arrays.asList(pwdColumnConfig, cardColumnConfig));
    }
    
    private EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final String logicalName, final String encryptorName) {
        return new EncryptColumnRuleConfiguration(logicalName, new EncryptColumnItemRuleConfiguration(logicalName, encryptorName));
    }
    
    private Map<String, AlgorithmConfiguration> getEncryptors(final AlgorithmConfiguration standardEncryptConfig, final AlgorithmConfiguration queryAssistedEncryptConfig,
                                                              final AlgorithmConfiguration queryLikeEncryptConfig) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(3, 1F);
        result.put("standard_encryptor", standardEncryptConfig);
        result.put("assisted_encryptor", queryAssistedEncryptConfig);
        result.put("like_encryptor", queryLikeEncryptConfig);
        return result;
    }
}
