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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import org.apache.shardingsphere.test.e2e.sql.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLExecuteType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

/**
 * Arguments provider for issue #28841 HintManager column-label regression (MySQL + JDBC + readwrite_splitting only).
 */
public final class HintManagerColumnLabelArgumentsProvider implements ArgumentsProvider {
    
    public static final String TARGET_SQL = "SELECT T.*, T.status status_new FROM t_order T WHERE T.order_id = 1000";
    
    public static final String TARGET_SCENARIO = "readwrite_splitting";
    
    /**
     * write_ds hosts physical {@code t_order}; tbl scenario only has {@code t_order_0..9} and breaks data-source hint routing.
     */
    public static final String HINT_DATA_SOURCE_NAME = "write_ds";
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        return E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DQL).stream()
                .filter(HintManagerColumnLabelArgumentsProvider::isTargetCase)
                .map(Arguments::of);
    }
    
    private static boolean isTargetCase(final AssertionTestParameter testParam) {
        if (null == testParam.getTestCaseContext() || null == testParam.getTestCaseContext().getTestCase().getSql()) {
            return false;
        }
        return TARGET_SQL.equals(testParam.getTestCaseContext().getTestCase().getSql())
                && "MySQL".equals(testParam.getDatabaseType().getType())
                && "jdbc".equals(testParam.getAdapter())
                && TARGET_SCENARIO.equals(testParam.getScenario())
                && SQLExecuteType.LITERAL == testParam.getSqlExecuteType();
    }
}
