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

package org.apache.shardingsphere.test.e2e.sql.framework;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.e2e.sql.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.E2ETestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * SQL E2E IT arguments provider.
 */
public final class SQLE2EITArgumentsProvider implements ArgumentsProvider {
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        SQLE2EITSettings settings = context.getRequiredTestClass().getAnnotation(SQLE2EITSettings.class);
        Preconditions.checkNotNull(settings, "Annotation `%s` is required.", SQLE2EITSettings.class.getSimpleName());
        return settings.batch() ? getBatchTestCaseArguments(settings.value()) : getSingleTestCaseArguments(settings.value());
    }
    
    private Stream<Arguments> getBatchTestCaseArguments(final SQLCommandType type) {
        Collection<E2ETestParameter> result = E2ETestParameterFactory.getCaseTestParameters(type);
        // TODO make sure test case can not be null
        return result.isEmpty() ? Stream.of(Arguments.of(new CaseTestParameter(null, null, null, null, null, null))) : result.stream().map(Arguments::of);
    }
    
    private Stream<Arguments> getSingleTestCaseArguments(final SQLCommandType type) {
        Collection<AssertionTestParameter> result = E2ETestParameterFactory.getAssertionTestParameters(type);
        // TODO make sure test case can not be null
        return result.isEmpty() ? Stream.of(Arguments.of(new AssertionTestParameter(null, null, null, null, null, null, null, null))) : result.stream().map(Arguments::of);
    }
}
