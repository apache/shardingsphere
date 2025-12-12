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

package org.apache.shardingsphere.infra.rewrite.parameter.builder.impl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GroupedParameterBuilderTest {
    
    @Test
    void assertGetParameters() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), Collections.emptyList(), new LinkedList<>());
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6)));
    }
    
    @Test
    void assertGetParametersWithGenericParameters() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), Collections.emptyList(), createGenericParameters());
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6, 7, 8)));
        assertThat(actual.getAfterGenericParameterBuilder().getParameters(), is(Arrays.<Object>asList(7, 8)));
    }
    
    @Test
    void assertGetGenericParametersWithModify() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(new LinkedList<>(), Collections.emptyList(), createGenericParameters());
        actual.getAfterGenericParameterBuilder().addReplacedParameters(0, 77);
        actual.getAfterGenericParameterBuilder().addReplacedParameters(1, 88);
        actual.getAfterGenericParameterBuilder().addAddedParameters(0, Arrays.asList(66, -1));
        actual.getAfterGenericParameterBuilder().addAddedParameters(2, Arrays.asList(99, 110));
        assertThat(actual.getAfterGenericParameterBuilder().getParameters(), is(Arrays.<Object>asList(77, 66, -1, 88, 99, 110)));
    }
    
    @Test
    void assertGetDerivedColumnName() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), Collections.emptyList(), createGenericParameters());
        String derivedColumnName = "derivedColumnName";
        actual.setDerivedColumnName(derivedColumnName);
        assertThat(actual.getDerivedColumnName(), is(Optional.of(derivedColumnName)));
    }
    
    private List<Object> createGenericParameters() {
        return new LinkedList<>(Arrays.asList(7, 8));
    }
    
    private List<List<Object>> createGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        result.add(Arrays.asList(3, 4));
        result.add(Arrays.asList(5, 6));
        return result;
    }
}
