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

package org.apache.shardingsphere.core.routing;

import org.apache.shardingsphere.core.routing.router.DatabaseHintSQLRouterTest;
import org.apache.shardingsphere.core.routing.type.broadcast.DatabaseBroadcastRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.broadcast.TableBroadcastRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.defaultdb.DefaultDatabaseRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.hint.DatabaseHintRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.ignore.IgnoreRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.standard.SQLRouteTest;
import org.apache.shardingsphere.core.routing.type.standard.StandardRoutingEngineTest;
import org.apache.shardingsphere.core.routing.type.standard.SubqueryRouteTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        DatabaseTest.class,
        DatabaseHintSQLRouterTest.class,
        DatabaseBroadcastRoutingEngineTest.class,
        TableBroadcastRoutingEngineTest.class,
        DefaultDatabaseRoutingEngineTest.class,
        DatabaseHintRoutingEngineTest.class,
        IgnoreRoutingEngineTest.class,
        StandardRoutingEngineTest.class,
        SubqueryRouteTest.class,
        SQLRouteTest.class,
        GeneratedKeyTest.class
})
public final class AllRoutingTests {
}
