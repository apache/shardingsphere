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

package org.apache.shardingsphere.infra.url.core.arg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class URLArgumentLineTest {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    private final String name = "fixture.config.driver.jdbc-url";
    private final String defaultValue = "jdbc-url";
    private final String line = String.format("%s=$${%s::%s}", name, name, defaultValue);

    @Test
    void assertParse() throws Exception {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(line);
        matcher.find();

        URLArgumentLine actual = URLArgumentLine.parse(line).get();

        assertThat(getURLArgumentLineField("argName").get(actual), is(name));
        assertThat(getURLArgumentLineField("argDefaultValue").get(actual), is(defaultValue));
        assertThat(getURLArgumentLineField("placehodlerMatcher").get(actual).toString(), is(matcher.toString()));
    }

    @Test
    void assertParseInvalidPattern() {
        final String line = "invalid-value";
        Optional<URLArgumentLine> actual = URLArgumentLine.parse(line);

        assertThat(actual, is(Optional.empty()));
    }

    @Test
    void assertReplaceArgument() {
        String actualEnvironmentArg = URLArgumentLine.parse(line).get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT);
//        String actualSystemPropsArg = URLArgumentLine.parse(line).get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS);

        assertThat(actualEnvironmentArg, is("fixture.config.driver.jdbc-url=jdbc-url"));
//        assertThat(actualSystemPropsArg, is("fixture.config.driver.jdbc-url=jdbc-url"));
    }

    private Field getURLArgumentLineField(String name) throws NoSuchFieldException {
        Field field = URLArgumentLine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
