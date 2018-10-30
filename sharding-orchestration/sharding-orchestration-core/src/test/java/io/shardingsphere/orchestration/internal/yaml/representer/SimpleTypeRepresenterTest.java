/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.yaml.representer;

import io.shardingsphere.orchestration.internal.yaml.representer.fixture.SimpleTypeRepresenterFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class SimpleTypeRepresenterTest {
    
    @Test
    public void assertToYamlWithoutSkipProperties() {
        String expected = new Yaml(new SimpleTypeRepresenter()).dumpAsMap(new SimpleTypeRepresenterFixture());
        assertCommonProperties(expected);
        assertThat(expected, containsString("skippedProperty: skipped\n"));
    }
    
    @Test
    public void assertToYamlWithSkipProperties() {
        String expected = new Yaml(new SimpleTypeRepresenter("skippedProperty")).dumpAsMap(new SimpleTypeRepresenterFixture());
        assertCommonProperties(expected);
        assertFalse(expected.contains("skippedProperty: skipped\n"));
    }
    
    private void assertCommonProperties(final String expected) {
        assertThat(expected, containsString("booleanValue: false\n"));
        assertThat(expected, containsString("booleanObjectValue: true\n"));
        assertThat(expected, containsString("intValue: 0\n"));
        assertThat(expected, containsString("integerObjectValue: 10\n"));
        assertThat(expected, containsString("longValue: 0\n"));
        assertThat(expected, containsString("longObjectValue: 10\n"));
        assertThat(expected, containsString("string: value\n"));
        assertFalse(expected.contains("collection:\n- value1\n- value2\n"));
    }
}
