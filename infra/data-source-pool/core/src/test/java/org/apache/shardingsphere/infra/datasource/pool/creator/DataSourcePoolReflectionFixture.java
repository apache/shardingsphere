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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

final class DataSourcePoolReflectionFixture implements DataSource {
    
    @Setter
    private String url;
    
    @Setter
    private String stringValue = "initial_string";
    
    @Setter
    private Integer integerValue;
    
    @Setter
    private Long longValue;
    
    @Getter
    @Setter
    private Boolean enabled;
    
    @Getter
    @Setter
    private List<String> listValue;
    
    @Getter
    @Setter
    private Properties jdbcUrlProperties;
    
    @Setter
    private Object customValue;
    
    @Setter
    private Duration connectionTimeout;
    
    @Getter
    @Setter
    private int loginTimeout;
    
    @Setter
    private boolean throwOnGetStringValue;
    
    /**
     * Get string value.
     *
     * @return string value
     * @throws ReflectiveOperationException reflective operation exception
     */
    public String getStringValue() throws ReflectiveOperationException {
        if (throwOnGetStringValue) {
            throw new ReflectiveOperationException("mocked getter failure");
        }
        return stringValue;
    }
    
    /**
     * Set primitive integer value.
     *
     * @param primitiveIntegerValue primitive integer value
     */
    public void setPrimitiveIntegerValue(final int primitiveIntegerValue) {
        integerValue = primitiveIntegerValue;
    }
    
    /**
     * Set primitive long value.
     *
     * @param primitiveLongValue primitive long value
     */
    public void setPrimitiveLongValue(final long primitiveLongValue) {
        longValue = primitiveLongValue;
    }
    
    /**
     * Set primitive enabled value.
     *
     * @param primitiveEnabled primitive enabled value
     */
    public void setPrimitiveEnabled(final boolean primitiveEnabled) {
        enabled = primitiveEnabled;
    }
    
    /**
     * Get null value.
     *
     * @return null value
     */
    public String getNullValue() {
        return null;
    }
    
    /**
     * Get object value.
     *
     * @return object value
     */
    public Object getObjectValue() {
        return this;
    }
    
    /**
     * Get ignored value.
     *
     * @param value value
     * @return ignored value
     */
    public Object getIgnoredValue(final String value) {
        return value;
    }
    
    /**
     * Set broken value.
     *
     * @param brokenValue broken value
     * @param ignored ignored or not
     */
    public void setBrokenValue(final String brokenValue, final String ignored) {
    }
    
    @Override
    public Connection getConnection() {
        return null;
    }
    
    @Override
    public Connection getConnection(final String username, final String password) {
        return null;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new SQLException("Unsupported operation");
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) {
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getGlobal();
    }
}
