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

package io.shardingsphere.proxy.backend.resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Proxy jdbc resource factory class.
 *
 * @author zhaojun
 */
public class ProxyJDBCResourceFactory {
    
    /**
     * Create common statement concern class.
     *
     * @return {@code ProxyJDBCResource}
     */
    public static ProxyJDBCResource newResource() {
        return new ProxyJDBCResource(new ArrayList<Connection>(), new ArrayList<Statement>(), new CopyOnWriteArrayList<ResultSet>());
    }
    
    /**
     * Create prepare statement concern class.
     *
     * @return {@code ProxyPrepareJDBCResource}
     */
    public static ProxyPrepareJDBCResource newPrepareResource() {
        return new ProxyPrepareJDBCResource(new ArrayList<Connection>(), new ArrayList<PreparedStatement>(), new CopyOnWriteArrayList<ResultSet>());
    }
}
