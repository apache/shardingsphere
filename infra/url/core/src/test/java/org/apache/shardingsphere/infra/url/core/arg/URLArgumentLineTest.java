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

public class URLArgumentLineTest {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$\\{(.+::.*)}$");
    
    private static final String NAME = "fixture.config.driver.jdbc-url";
    
    private static final String DEFAULT_VALUE = "jdbc-url";
    
    @Test
    void assertParse() throws NoSuchFieldException, IllegalAccessException {
        String line = String.format("%s=$${%s::%s}", NAME, NAME, DEFAULT_VALUE);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(line);
        matcher.find();
        
        URLArgumentLine actual = URLArgumentLine.parse(line).get();
        
        assertThat(getURLArgumentLineField("argName").get(actual), is(NAME));
        assertThat(getURLArgumentLineField("argDefaultValue").get(actual), is(DEFAULT_VALUE));
        assertThat(getURLArgumentLineField("placehodlerMatcher").get(actual).toString(), is(matcher.toString()));
    }
    
    @Test
    void assertParseInvalidPattern() {
        final String line = "invalid-value";
        Optional<URLArgumentLine> actual = URLArgumentLine.parse(line);
        
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    void assertReplaceArgumentWithEnvironment() {
        String line = String.format("%s=$${%s::%s}", NAME, NAME, DEFAULT_VALUE);
        String actual = URLArgumentLine.parse(line).get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT);
        
        assertThat(actual, is(String.format("%s=%s", NAME, DEFAULT_VALUE)));
    }
    
    @Test
    void assertReplaceArgumentWithProperty() {
        String line = String.format("%s=$${%s::%s}", NAME, NAME, DEFAULT_VALUE);
        URLArgumentLine.setSystemProperty(NAME, DEFAULT_VALUE);
        String actual = URLArgumentLine.parse(line).get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS);
        
        assertThat(actual, is(String.format("%s=%s", NAME, DEFAULT_VALUE)));
    }
    
    @Test
    void assertReplaceArgumentWithNone() {
        String line = String.format("%s=$${%s::}", NAME, NAME);
        String actual = URLArgumentLine.parse(line).get().replaceArgument(URLArgumentPlaceholderType.NONE);
        
        assertThat(actual, is(NAME));
    }
    
    private Field getURLArgumentLineField(final String name) throws NoSuchFieldException {
        Field field = URLArgumentLine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
