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

package org.apache.shardingsphere.test.e2e.sql.framework.param.array;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.E2ETestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;

import java.util.Collection;
import java.util.Collections;

/**
 * JDBC test parameter generator for standalone mode.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JdbcStandaloneTestParameterGenerator {
    
    private static final Collection<String> ADAPTERS = Collections.singleton(Adapter.JDBC.getValue());
    
    private static final E2ETestEnvironment ENV = E2ETestEnvironment.getInstance();
    
    /**
     * Get assertion test parameter.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameter
     */
    public static Collection<AssertionTestParameter> getAssertionTestParameter(final SQLCommandType sqlCommandType) {
        return new E2ETestParameterGenerator(ADAPTERS, ENV.getScenarios(), Mode.STANDALONE, ENV.getArtifactEnvironment().getDatabaseTypes(), ENV.getRunEnvironment().isRunSmokeCases())
                .getAssertionTestParameter(sqlCommandType);
    }
    
    /**
     * Get case test parameter.
     *
     * @param sqlCommandType SQL command type
     * @return case test parameter
     */
    public static Collection<E2ETestParameter> getCaseTestParameter(final SQLCommandType sqlCommandType) {
        return new E2ETestParameterGenerator(ADAPTERS, ENV.getScenarios(), Mode.STANDALONE, ENV.getArtifactEnvironment().getDatabaseTypes(), ENV.getRunEnvironment().isRunSmokeCases())
                .getCaseTestParameter(sqlCommandType);
    }
}
