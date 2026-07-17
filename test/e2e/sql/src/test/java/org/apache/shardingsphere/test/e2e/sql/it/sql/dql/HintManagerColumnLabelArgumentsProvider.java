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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.e2e.sql.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLExecuteType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Arguments provider for HintManager column label DQL cases.
 */
public final class HintManagerColumnLabelArgumentsProvider implements ArgumentsProvider {
    
    private static final String JOIN_SHORTHAND_SQL_PREFIX = "SELECT o.user_id, i.* FROM t_order o JOIN t_order_item i";
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        Collection<AssertionTestParameter> matched = E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DQL).stream()
                .filter(HintManagerColumnLabelArgumentsProvider::isTargetCase)
                .collect(Collectors.toList());
        Preconditions.checkState(!matched.isEmpty(),
                "HintManager column label case not found. Check e2e-dql-select-join.xml contains "
                        + "`SELECT o.user_id, i.* FROM t_order o JOIN t_order_item i ...` "
                        + "and e2e-env.properties is tbl + MySQL + jdbc.");
        return matched.stream().map(Arguments::of);
    }
    
    private static boolean isTargetCase(final AssertionTestParameter testParam) {
        if (null == testParam.getTestCaseContext() || null == testParam.getTestCaseContext().getTestCase().getSql()) {
            return false;
        }
        String sql = testParam.getTestCaseContext().getTestCase().getSql();
        return sql.contains(JOIN_SHORTHAND_SQL_PREFIX)
                && "MySQL".equals(testParam.getDatabaseType().getType())
                && "jdbc".equals(testParam.getAdapter())
                && "tbl".equals(testParam.getScenario())
                && SQLExecuteType.PLACEHOLDER == testParam.getSqlExecuteType();
    }
}
