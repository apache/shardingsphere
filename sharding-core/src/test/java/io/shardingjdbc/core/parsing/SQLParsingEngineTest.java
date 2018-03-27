/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.base.AbstractBaseParseSQLTest;
import io.shardingjdbc.core.parsing.parser.base.AbstractBaseParseTest;
import io.shardingjdbc.core.parsing.parser.jaxb.Assert;
import io.shardingjdbc.core.parsing.parser.jaxb.helper.ParserJAXBHelper;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.util.SQLPlaceholderUtil;
import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@RunWith(Parameterized.class)
public final class SQLParsingEngineTest extends AbstractBaseParseSQLTest {
    
    private final String[] parameters;
    
    public SQLParsingEngineTest(final String testCaseName, final DatabaseType databaseType, final Assert assertObj) {
        super(testCaseName, databaseType, assertObj);
        parameters = ParserJAXBHelper.getParameters(assertObj.getParameters());
    }
    
    @Parameters(name = "{0}In{1}")
    public static Collection<Object[]> dataParameters() {
        return AbstractBaseParseTest.dataParameters();
    }
    
    @Test
    public void assertStatement() throws IOException {
        assertStatement(new SQLParsingEngine(getDatabaseType(), SQLPlaceholderUtil.replaceStatement(SQLCasesLoader.getInstance().getSQL(getTestCaseName()), parameters), buildShardingRule()).parse());
    }
    
    @Test
    public void assertPreparedStatement() throws IOException {
        for (DatabaseType each : getDataBaseTypes(SQLCasesLoader.getInstance().getDatabaseTypes(getTestCaseName()))) {
            assertPreparedStatement(new SQLParsingEngine(each, SQLPlaceholderUtil.replacePreparedStatement(SQLCasesLoader.getInstance().getSQL(getTestCaseName())), buildShardingRule()).parse());
        }
    }
    
    private ShardingRule buildShardingRule() throws IOException {
        YamlShardingConfiguration yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(SQLParsingEngineTest.class.getClassLoader().getResource("yaml/parser-rule.yaml").getFile()));
        return yamlShardingConfig.getShardingRule(yamlShardingConfig.getDataSources().keySet());
    }
    
    private static Collection<DatabaseType> getDataBaseTypes(final Collection<String> databaseTypes) {
        if (databaseTypes.isEmpty()) {
            return Arrays.asList(DatabaseType.values());
        }
        Collection<DatabaseType> result = new LinkedHashSet<>(databaseTypes.size());
        for (String each : databaseTypes) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
}
