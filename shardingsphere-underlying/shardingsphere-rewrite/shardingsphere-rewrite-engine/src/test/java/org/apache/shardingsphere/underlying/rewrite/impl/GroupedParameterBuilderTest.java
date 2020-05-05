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

package org.apache.shardingsphere.underlying.rewrite.impl;

import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GroupedParameterBuilderTest {
    
    @Test
    public void assertGetParameters() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), new ArrayList<>());
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6)));
    }

    @Test
    public void assertGetParametersWithOnDuplicateKeyParameters() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), createOnDuplicateKeyUpdateParameters());
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6, 7, 8)));
    }

    @Test
    public void assertGetParametersWithOnDuplicateKeyParametersWithModify() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), createOnDuplicateKeyUpdateParameters());
        actual.addReplacedIndexAndOnDuplicateKeyUpdateParameters(0, 77);
        actual.addReplacedIndexAndOnDuplicateKeyUpdateParameters(1, 88);
        actual.getAddedIndexAndOnDuplicateKeyParameters().put(2, Arrays.asList(99, 110));
        actual.getAddedIndexAndOnDuplicateKeyParameters().put(0, Collections.singletonList(66));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6, 66, 77, 88, 99, 110)));
    }

    @Test
    public void assertGetDerivedColumnName() {
        GroupedParameterBuilder actual = new GroupedParameterBuilder(createGroupedParameters(), createOnDuplicateKeyUpdateParameters());
        String derivedColumnName = "derivedColumnName";
        actual.setDerivedColumnName(derivedColumnName);
        assertThat(actual.getDerivedColumnName(), is(Optional.of(derivedColumnName)));
    }

    private List<Object> createOnDuplicateKeyUpdateParameters() {
        LinkedList<Object> result = new LinkedList<>();
        result.add(7);
        result.add(8);
        return result;
    }

    private List<List<Object>> createGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        result.add(Arrays.asList(3, 4));
        result.add(Arrays.asList(5, 6));
        return result;
    }
}
