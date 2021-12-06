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

package org.apache.shardingsphere.sharding.rewrite.parameterized.scenario;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String CASE_PATH = "scenario/encrypt/case";
    
    public EncryptSQLRewriterParameterizedTest(final String type, final String name, final String fileName, 
                                               final String databaseType, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} ({3}) -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(CASE_PATH.toUpperCase(), CASE_PATH, EncryptSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected YamlRootConfiguration createRootConfiguration() throws IOException {
        URL url = EncryptSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected ShardingSphereSchema mockSchema() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_detail")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        return result;
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules) {
        Optional<SingleTableRule> singleTableRule = rules.stream().filter(each -> each instanceof SingleTableRule).map(each -> (SingleTableRule) each).findFirst();
        if (singleTableRule.isPresent()) {
            singleTableRule.get().put("t_account", "encrypt_ds");
            singleTableRule.get().put("t_account_bak", "encrypt_ds");
            singleTableRule.get().put("t_account_detail", "encrypt_ds");
        }
    }
}
