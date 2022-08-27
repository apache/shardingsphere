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

package org.apache.shardingsphere.test.integration.framework.param.array;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import java.util.Collection;
import java.util.Collections;

/**
 * Parameterized array generator for standalone mode.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StandaloneParameterizedArrayGenerator {
    
    private static final Collection<String> ADAPTERS = Collections.singleton("jdbc");
    
    private static final Collection<DatabaseType> DATABASE_TYPES = Collections.singleton(DatabaseTypeFactory.getInstance("H2"));
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    /**
     * Get assertion parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return assertion parameterized array
     */
    public static Collection<AssertionParameterizedArray> getAssertionParameterized(final SQLCommandType sqlCommandType) {
        return new ParameterizedArrayGenerator(ADAPTERS, ENV.getScenarios(), "Standalone", DATABASE_TYPES).getAssertionParameterized(sqlCommandType);
    }
    
    /**
     * Get case parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return case parameterized array
     */
    public static Collection<ParameterizedArray> getCaseParameterized(final SQLCommandType sqlCommandType) {
        return new ParameterizedArrayGenerator(ADAPTERS, ENV.getScenarios(), "Standalone", DATABASE_TYPES).getCaseParameterized(sqlCommandType);
    }
}
