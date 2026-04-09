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

package org.apache.shardingsphere.infra.datasource.pool.hikari.metadata;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.validator.DataSourcePoolPropertiesContentValidator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HikariDataSourcePoolPropertiesContentValidatorTest {
    
    private final DataSourcePoolPropertiesContentValidator validator = TypedSPILoader.getService(DataSourcePoolPropertiesContentValidator.class, HikariDataSource.class.getName());
    
    @Test
    void assertValidateWithNoProperties() {
        assertDoesNotThrow(() -> validator.validate(mockDataSourcePoolProperties(Collections.emptyMap())));
    }
    
    @Test
    void assertValidateWithNullPropertyValue() {
        assertDoesNotThrow(() -> validator.validate(mockDataSourcePoolProperties(Collections.singletonMap("connectionTimeout", null))));
    }
    
    @Test
    void assertValidateWithZeroKeepaliveTime() {
        assertDoesNotThrow(() -> validator.validate(mockDataSourcePoolProperties(Collections.singletonMap("keepaliveTime", 0L))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validBoundaryValueArguments")
    void assertValidateWithValidBoundaryValue(final String name, final Map<String, Object> localProperties) {
        assertDoesNotThrow(() -> validator.validate(mockDataSourcePoolProperties(localProperties)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBoundaryValueArguments")
    void assertValidateWithInvalidBoundaryValue(final String name, final Map<String, Object> localProperties, final String expectedMessage) {
        assertThat(assertThrows(IllegalStateException.class, () -> validator.validate(mockDataSourcePoolProperties(localProperties))).getMessage(), is(expectedMessage));
    }
    
    private DataSourcePoolProperties mockDataSourcePoolProperties(final Map<String, Object> localProperties) {
        DataSourcePoolProperties result = mock(DataSourcePoolProperties.class);
        when(result.getAllLocalProperties()).thenReturn(localProperties);
        return result;
    }
    
    private static Stream<Arguments> validBoundaryValueArguments() {
        return Stream.of(
                Arguments.of("connectionTimeout at minimum", Collections.singletonMap("connectionTimeout", 250L)),
                Arguments.of("idleTimeout at minimum", Collections.singletonMap("idleTimeout", 0L)),
                Arguments.of("maxLifetime at minimum", Collections.singletonMap("maxLifetime", 30_000L)),
                Arguments.of("maximumPoolSize at minimum", Collections.singletonMap("maximumPoolSize", 1)),
                Arguments.of("minimumIdle at minimum", Collections.singletonMap("minimumIdle", 0)),
                Arguments.of("keepaliveTime at minimum", Collections.singletonMap("keepaliveTime", 30_000L)));
    }
    
    private static Stream<Arguments> invalidBoundaryValueArguments() {
        return Stream.of(
                Arguments.of("connectionTimeout below minimum", Collections.singletonMap("connectionTimeout", 249L), "connectionTimeout can not less than 250 ms."),
                Arguments.of("idleTimeout negative", Collections.singletonMap("idleTimeout", -1L), "idleTimeout can not be negative."),
                Arguments.of("maxLifetime below minimum", Collections.singletonMap("maxLifetime", 29_999L), "maxLifetime can not less than 30000 ms."),
                Arguments.of("maximumPoolSize below minimum", Collections.singletonMap("maximumPoolSize", 0), "maxPoolSize can not less than 1."),
                Arguments.of("minimumIdle negative", Collections.singletonMap("minimumIdle", -1), "minimumIdle can not be negative."),
                Arguments.of("keepaliveTime below minimum", Collections.singletonMap("keepaliveTime", 29_999L), "keepaliveTime can not be less than 30000 ms."));
    }
}
