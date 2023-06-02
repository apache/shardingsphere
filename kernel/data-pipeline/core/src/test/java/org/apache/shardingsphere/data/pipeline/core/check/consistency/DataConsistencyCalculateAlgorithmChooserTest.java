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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataConsistencyCalculateAlgorithmChooserTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertChooseOnDifferentDatabaseTypes(final String name, final String databaseTypeName, final String peerDatabaseTypeName, final String expectedDataConsistencyType) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
        DatabaseType peerDatabaseType = TypedSPILoader.getService(DatabaseType.class, peerDatabaseTypeName);
        assertThat(DataConsistencyCalculateAlgorithmChooser.choose(databaseType, peerDatabaseType).getType(), is(expectedDataConsistencyType));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("chooseOnDifferentDatabaseTypes", "Oracle", "PostgreSQL", "DATA_MATCH"),
                    Arguments.of("chooseOnMySQL", "MySQL", "MySQL", "CRC32_MATCH"),
                    Arguments.of("chooseOnPostgreSQL", "PostgreSQL", "PostgreSQL", "DATA_MATCH"));
        }
    }
}
