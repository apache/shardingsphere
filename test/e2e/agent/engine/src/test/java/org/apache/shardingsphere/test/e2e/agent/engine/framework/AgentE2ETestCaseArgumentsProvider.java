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

package org.apache.shardingsphere.test.e2e.agent.engine.framework;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.agent.engine.cases.AgentE2ETestCases;
import org.apache.shardingsphere.test.e2e.agent.engine.cases.AgentE2ETestCasesLoader;
import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

/**
 * Agent E2E test case arguments provider.
 */
@RequiredArgsConstructor
public abstract class AgentE2ETestCaseArgumentsProvider implements ArgumentsProvider {
    
    private final Class<? extends AgentE2ETestCases<?>> agentE2ETestCasesClass;
    
    @Override
    public final Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        return new AgentE2ETestCasesLoader(agentE2ETestCasesClass).loadTestCases(AgentE2ETestConfiguration.getInstance().getAdapter()).stream().map(Arguments::of);
    }
}
