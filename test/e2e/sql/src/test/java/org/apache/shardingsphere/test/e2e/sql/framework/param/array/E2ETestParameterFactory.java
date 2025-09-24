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
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.E2ETestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;

import java.util.Collection;
import java.util.LinkedList;

/**
 * E2E test parameter factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class E2ETestParameterFactory {
    
    private static final ArtifactEnvironment ENV = E2ETestEnvironment.getInstance().getArtifactEnvironment();
    
    /**
     * Get assertion test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameters
     */
    public static Collection<AssertionTestParameter> getAssertionTestParameters(final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (Mode each : ENV.getModes()) {
            if (Mode.STANDALONE == each) {
                result.addAll(isDistSQLCommandType(sqlCommandType)
                        ? ProxyStandaloneTestParameterGenerator.getAssertionTestParameter(sqlCommandType)
                        : JdbcStandaloneTestParameterGenerator.getAssertionTestParameter(sqlCommandType));
            } else if (Mode.CLUSTER == each) {
                result.addAll(ClusterTestParameterArrayGenerator.getAssertionTestParameter(sqlCommandType));
            }
        }
        return result;
    }
    
    /**
     * Get case test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return case test parameters
     */
    public static Collection<E2ETestParameter> getCaseTestParameters(final SQLCommandType sqlCommandType) {
        Collection<E2ETestParameter> result = new LinkedList<>();
        for (Mode each : ENV.getModes()) {
            if (Mode.STANDALONE == each) {
                result.addAll(isDistSQLCommandType(sqlCommandType)
                        ? ProxyStandaloneTestParameterGenerator.getCaseTestParameter(sqlCommandType)
                        : JdbcStandaloneTestParameterGenerator.getCaseTestParameter(sqlCommandType));
            } else if (Mode.CLUSTER == each) {
                result.addAll(ClusterTestParameterArrayGenerator.getCaseTestParameter(sqlCommandType));
            }
        }
        return result;
    }
    
    private static boolean isDistSQLCommandType(final SQLCommandType sqlCommandType) {
        return SQLCommandType.RDL == sqlCommandType || SQLCommandType.RAL == sqlCommandType || SQLCommandType.RQL == sqlCommandType;
    }
}
