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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.datasource;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link HikariMonitor}.
 */
class HikariMonitorTest {
    
    private final HikariMonitor hikariMonitor = new HikariMonitor();
    
    @Test
    void assertIsBlankWithNull() throws Exception {
        assertTrue(invokePrivateIsBlank(null));
    }
    
    @Test
    void assertIsBlankWithEmptyString() throws Exception {
        assertTrue(invokePrivateIsBlank(""));
    }
    
    @Test
    void assertIsBlankWithBlankString() throws Exception {
        assertTrue(invokePrivateIsBlank("   "));
    }
    
    @Test
    void assertIsBlankWithNonBlankString() throws Exception {
        assertFalse(invokePrivateIsBlank("9090"));
    }
    
    @Test
    void assertIsValidPortWithValidPort() throws Exception {
        assertTrue(invokePrivateIsValidPort("9090"));
    }
    
    @Test
    void assertIsValidPortWithMinValidPort() throws Exception {
        assertTrue(invokePrivateIsValidPort("1"));
    }
    
    @Test
    void assertIsValidPortWithMaxValidPort() throws Exception {
        assertTrue(invokePrivateIsValidPort("65535"));
    }
    
    @Test
    void assertIsValidPortWithPortBelowMin() throws Exception {
        assertFalse(invokePrivateIsValidPort("0"));
    }
    
    @Test
    void assertIsValidPortWithPortAboveMax() throws Exception {
        assertFalse(invokePrivateIsValidPort("65536"));
    }
    
    @Test
    void assertIsValidPortWithNegativePort() throws Exception {
        assertFalse(invokePrivateIsValidPort("-1"));
    }
    
    @Test
    void assertIsValidPortWithInvalidNumber() throws Exception {
        assertFalse(invokePrivateIsValidPort("abc"));
    }
    
    @Test
    void assertIsValidPortWithDecimalNumber() throws Exception {
        assertFalse(invokePrivateIsValidPort("90.90"));
    }
    
    @Test
    void assertToDoubleSafelyWithNull() throws Exception {
        double result = invokePrivateToDoubleSafely(null);
        assertThat(result, is(0.0));
    }
    
    @Test
    void assertToDoubleSafelyWithInteger() throws Exception {
        double result = invokePrivateToDoubleSafely(123);
        assertThat(result, is(123.0));
    }
    
    @Test
    void assertToDoubleSafelyWithLong() throws Exception {
        double result = invokePrivateToDoubleSafely(456L);
        assertThat(result, is(456.0));
    }
    
    @Test
    void assertToDoubleSafelyWithDouble() throws Exception {
        double result = invokePrivateToDoubleSafely(789.12);
        assertThat(result, is(789.12));
    }
    
    @Test
    void assertToDoubleSafelyWithFloat() throws Exception {
        double result = invokePrivateToDoubleSafely(34.56f);
        assertThat(result, is(34.560001373291016));
    }
    
    @Test
    void assertToDoubleSafelyWithStringNumber() throws Exception {
        double result = invokePrivateToDoubleSafely("456.78");
        assertThat(result, is(456.78));
    }
    
    @Test
    void assertToDoubleSafelyWithIntegerString() throws Exception {
        double result = invokePrivateToDoubleSafely("123");
        assertThat(result, is(123.0));
    }
    
    @Test
    void assertToDoubleSafelyWithInvalidString() throws Exception {
        double result = invokePrivateToDoubleSafely("invalid");
        assertThat(result, is(0.0));
    }
    
    @Test
    void assertToDoubleSafelyWithEmptyString() throws Exception {
        double result = invokePrivateToDoubleSafely("");
        assertThat(result, is(0.0));
    }
    
    @Test
    void assertExtractPoolNameWithNull() throws Exception {
        String result = invokePrivateExtractPoolName(null);
        assertThat(result, is("unknown"));
    }
    
    @Test
    void assertExtractPoolNameWithPoolPattern() throws Exception {
        String result = invokePrivateExtractPoolName("Pool (test-pool)");
        assertThat(result, is("test-pool"));
    }
    
    @Test
    void assertExtractPoolNameWithPoolConfigPattern() throws Exception {
        String result = invokePrivateExtractPoolName("PoolConfig (test-pool)");
        assertThat(result, is("test-pool"));
    }
    
    @Test
    void assertExtractPoolNameWithPoolPatternWithSpaces() throws Exception {
        String result = invokePrivateExtractPoolName("Pool  (  test-pool  )");
        assertThat(result, is("  test-pool  "));
    }
    
    @Test
    void assertExtractPoolNameWithPoolConfigPatternWithSpaces() throws Exception {
        String result = invokePrivateExtractPoolName("PoolConfig  (  test-pool  )");
        assertThat(result, is("  test-pool  "));
    }
    
    @Test
    void assertExtractPoolNameWithUnknownPattern() throws Exception {
        String input = "UnknownPattern";
        String result = invokePrivateExtractPoolName(input);
        assertThat(result, is(input));
    }
    
    @Test
    void assertExtractPoolNameWithEmptyPoolName() throws Exception {
        String result = invokePrivateExtractPoolName("Pool ()");
        assertThat(result, is(""));
    }
    
    @Test
    void assertExtractPoolNameWithEmptyPoolConfigName() throws Exception {
        String result = invokePrivateExtractPoolName("PoolConfig ()");
        assertThat(result, is(""));
    }
    
    @Test
    void assertStartScheduleMonitorWithBlankPort() {
        hikariMonitor.startScheduleMonitor("   ");
        // Should not throw exception and log warning
    }
    
    @Test
    void assertStartScheduleMonitorWithNullPort() {
        hikariMonitor.startScheduleMonitor(null);
        // Should not throw exception and log warning
    }
    
    @Test
    void assertStartScheduleMonitorWithInvalidPort() {
        hikariMonitor.startScheduleMonitor("invalid");
        // Should not throw exception and log error
    }
    
    @Test
    void assertStartScheduleMonitorWithOutOfRangePort() {
        hikariMonitor.startScheduleMonitor("70000");
        // Should not throw exception and log error
    }
    
    @Test
    void assertSingletonShutdownHookRegistration() throws Exception {
        // Test that multiple instances don't register multiple shutdown hooks
        HikariMonitor monitor1 = new HikariMonitor();
        HikariMonitor monitor2 = new HikariMonitor();
        
        // This is difficult to test without actually running the shutdown hook,
        // but we can verify the method doesn't throw exceptions
        monitor1.startScheduleMonitor("9090");
        monitor2.startScheduleMonitor("9091");
        
        // Should not throw ConcurrentModificationException or other errors
    }
    
    @Test
    void assertDoubleStartProtection() {
        hikariMonitor.startScheduleMonitor("9090");
        hikariMonitor.startScheduleMonitor("9090");
        
        // Should not throw exception and log warning about duplicate call
    }
    
    // Helper methods to access private methods via reflection
    private boolean invokePrivateIsBlank(final String str) throws Exception {
        Method method = HikariMonitor.class.getDeclaredMethod("isBlank", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(hikariMonitor, str);
    }
    
    private boolean invokePrivateIsValidPort(final String portStr) throws Exception {
        Method method = HikariMonitor.class.getDeclaredMethod("isValidPort", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(hikariMonitor, portStr);
    }
    
    private double invokePrivateToDoubleSafely(final Object value) throws Exception {
        Method method = HikariMonitor.class.getDeclaredMethod("toDoubleSafely", Object.class);
        method.setAccessible(true);
        return (double) method.invoke(hikariMonitor, value);
    }
    
    private String invokePrivateExtractPoolName(final String typeName) throws Exception {
        Method method = HikariMonitor.class.getDeclaredMethod("extractPoolName", String.class);
        method.setAccessible(true);
        return (String) method.invoke(hikariMonitor, typeName);
    }
}
