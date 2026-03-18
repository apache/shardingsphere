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

package org.apache.shardingsphere.mask.algorithm.parameterized.execute;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class MaskAlgorithmExecuteArgumentsProvider implements ArgumentsProvider {
    
    private final String type;
    
    private final Properties props;
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        return getCaseAsserts().stream().map(each -> Arguments.of(type, each.getName(), props, each.getPlainValue(), each.getMaskedValue()));
    }
    
    protected abstract Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts();
}
