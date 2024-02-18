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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class URLArgumentLineRenderTest {
    
    private static final String FIXTURE_JDBC_URL_KEY = "fixture.config.driver.jdbc-url";
    
    private static final String FIXTURE_USERNAME_KEY = "fixture.config.driver.username";
    
    @BeforeAll
    static void beforeAll() {
        System.setProperty(FIXTURE_JDBC_URL_KEY, "jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        System.setProperty(FIXTURE_USERNAME_KEY, "sa");
    }
    
    @AfterAll
    static void afterAll() {
        System.clearProperty(FIXTURE_JDBC_URL_KEY);
        System.clearProperty(FIXTURE_USERNAME_KEY);
    }
    
    @Test
    void assertReadWithNonePlaceholder() throws IOException, URISyntaxException {
        byte[] actual = readContent("config/to-be-replaced-fixture.yaml", URLArgumentPlaceholderType.NONE);
        byte[] expected = readContent("config/to-be-replaced-fixture.yaml", URLArgumentPlaceholderType.NONE);
        assertThat(new String(actual), is(new String(expected)));
    }
    
    @Test
    void assertReadWithSystemPropertiesPlaceholder() throws IOException, URISyntaxException {
        byte[] actual = readContent("config/to-be-replaced-fixture.yaml", URLArgumentPlaceholderType.SYSTEM_PROPS);
        byte[] expected = readContent("config/replaced-fixture.yaml", URLArgumentPlaceholderType.SYSTEM_PROPS);
        assertThat(new String(actual), is(new String(expected)));
    }
    
    private byte[] readContent(final String name, final URLArgumentPlaceholderType placeholderType) throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(name)).toURI().getPath());
        return URLArgumentLineRender.render(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream().filter(each -> !each.startsWith("#")).collect(Collectors.toList()), placeholderType);
    }
}
