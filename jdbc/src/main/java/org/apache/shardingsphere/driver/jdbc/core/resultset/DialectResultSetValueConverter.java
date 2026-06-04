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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Dialect result set value converter.
 */
@SingletonSPI
public interface DialectResultSetValueConverter extends TypedSPI {
    
    /**
     * Convert value via expected class type.
     *
     * @param value original value
     * @param convertType expected class type
     * @return converted value
     * @throws SQLFeatureNotSupportedException SQL feature not supported exception
     */
    Object convertValue(Object value, Class<?> convertType) throws SQLFeatureNotSupportedException;
}
