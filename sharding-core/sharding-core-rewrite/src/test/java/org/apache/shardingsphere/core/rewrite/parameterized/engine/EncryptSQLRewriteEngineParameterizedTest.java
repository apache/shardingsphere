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

package org.apache.shardingsphere.core.rewrite.parameterized.engine;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.preprocessor.SQLStatementContextFactory;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.engine.impl.DefaultSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.entity.EncryptRewriteAssertionEntity;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.entity.EncryptRewriteAssertionsRootEntity;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.loader.EncryptRewriteAssertionsRootEntityLoader;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlRootEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class EncryptSQLRewriteEngineParameterizedTest {
    
    private final String ruleFile;
    
    private final String name;
    
    private final String inputSQL;
    
    private final List<Object> inputParameters;
    
    private final String outputSQL;
    
    private final List<Object> outputParameters;
    
    private final String databaseType;
    
    @Parameters(name = "{1}")
    public static Collection<Object[]> getTestParameters() {
        Collection<Object[]> result = new LinkedList<>();
        for (EncryptRewriteAssertionsRootEntity each : getAllEncryptRewriteAssertionsRootEntities()) {
            result.addAll(getTestParameters(each));
        }
        return result;
    }
    
    private static Collection<Object[]> getTestParameters(final EncryptRewriteAssertionsRootEntity rootAssertions) {
        Collection<Object[]> result = new LinkedList<>();
        for (EncryptRewriteAssertionEntity each : rootAssertions.getAssertions()) {
            result.add(getTestParameter(rootAssertions, each));
        }
        return result;
    }
    
    private static Object[] getTestParameter(final EncryptRewriteAssertionsRootEntity rootAssertions, final EncryptRewriteAssertionEntity assertion) {
        Object[] result = new Object[7];
        result[0] = rootAssertions.getYamlRule();
        result[1] = assertion.getId();
        result[2] = assertion.getInput().getSql();
        result[3] = null == assertion.getInput().getParameters() ? Collections.emptyList() : Splitter.on(",").trimResults().splitToList(assertion.getInput().getParameters());
        result[4] = assertion.getOutput().getSql();
        result[5] = null == assertion.getOutput().getParameters() ? Collections.emptyList() : Splitter.on(",").trimResults().splitToList(assertion.getOutput().getParameters());
        result[6] = assertion.getDatabaseType();
        return result;
    }
    
    private static Collection<EncryptRewriteAssertionsRootEntity> getAllEncryptRewriteAssertionsRootEntities() {
        Collection<EncryptRewriteAssertionsRootEntity> result = new LinkedList<>();
        File file = new File(EncryptSQLRewriteEngineParameterizedTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/encrypt");
        for (File each : Objects.requireNonNull(file.listFiles())) {
            if (each.getName().endsWith(".xml")) {
                result.add(new EncryptRewriteAssertionsRootEntityLoader().load("encrypt/" + each.getName()));
            }
        }
        return result;
    }
    
    @Test
    public void assertRewrite() throws IOException {
        SQLRewriteResult actual = getSQLRewriteResult();
        assertThat(actual.getSql(), is(outputSQL));
        assertThat(actual.getParameters(), is(outputParameters));
    }
    
    private SQLRewriteResult getSQLRewriteResult() throws IOException {
        SQLRewriteContext sqlRewriteContext = getSQLRewriteContext();
        YamlRootEncryptRuleConfiguration encryptRuleConfiguration = createEncryptRuleConfiguration();
        EncryptRule encryptRule = new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration.getEncryptRule()));
        boolean isQueryWithCipherColumn = (boolean) encryptRuleConfiguration.getProps().get("query.with.cipher.column");
        new EncryptSQLRewriteContextDecorator(encryptRule, isQueryWithCipherColumn).decorate(sqlRewriteContext);
        return new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
    }
    
    private SQLRewriteContext getSQLRewriteContext() {
        SQLStatement sqlStatement = new SQLParseEngine(DatabaseTypes.getActualDatabaseType(null == databaseType ? "SQL92" : databaseType)).parse(inputSQL, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(mock(TableMetas.class), inputSQL, inputParameters, sqlStatement);
        return new SQLRewriteContext(mock(TableMetas.class), sqlStatementContext, inputSQL, inputParameters);
    }
    
    private YamlRootEncryptRuleConfiguration createEncryptRuleConfiguration() throws IOException {
        URL url = EncryptSQLRewriteEngineParameterizedTest.class.getClassLoader().getResource(ruleFile);
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootEncryptRuleConfiguration.class);
    }
}
