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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBaseSQLParsingEngineTest {
    
    @Getter(AccessLevel.PROTECTED)
    private static ShardingRule shardingRule;
    
    @BeforeClass
    public static void setUp() throws IOException {
        shardingRule = buildShardingRule();
    }
    
    private static ShardingRule buildShardingRule() throws IOException {
        URL url = SQLParsingEngineTest.class.getClassLoader().getResource("yaml/parser-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found parser rule yaml configuration.");
        YamlShardingConfiguration yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(url.getFile()));
        return yamlShardingConfig.getShardingRule(yamlShardingConfig.getDataSources().keySet());
    }
    
    protected static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        if (Strings.isNullOrEmpty(databaseTypes)) {
            return Sets.newHashSet(DatabaseType.values());
        }
        Set<DatabaseType> result = new HashSet<>();
        for (String each : databaseTypes.split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
}
