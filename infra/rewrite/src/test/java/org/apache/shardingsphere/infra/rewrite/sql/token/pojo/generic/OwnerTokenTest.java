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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class OwnerTokenTest {
    
    @Test
    public void assertOwnerTokenWithOwnerNameEqualsTableName() {
        OwnerToken ownerToken = new OwnerToken(0, 1, new IdentifierValue("t_user"), new IdentifierValue("t_user"));
        assertThat(ownerToken.toString(buildRouteUnit()), is("t_user_0."));
        assertTokenGrid(ownerToken);
    }
    
    @Test
    public void assertOwnerTokenWithOwnerNameNotEqualsTableName() {
        OwnerToken ownerToken = new OwnerToken(0, 1, new IdentifierValue("u"), new IdentifierValue("t_user"));
        assertThat(ownerToken.toString(buildRouteUnit()), is("u."));
        assertTokenGrid(ownerToken);
    }
    
    @Test
    public void assertOwnerTokenWithNoRouteUnitAndOwnerNameEqualsTableName() {
        OwnerToken ownerToken = new OwnerToken(0, 1, new IdentifierValue("t_user_detail"), new IdentifierValue("t_user_detail"));
        assertThat(ownerToken.toString(), is("t_user_detail."));
        assertTokenGrid(ownerToken);
    }
    
    @Test
    public void assertOwnerTokenWithNoRouteUnitAndOwnerNameNotEqualsTableName() {
        OwnerToken ownerToken = new OwnerToken(0, 1, new IdentifierValue("ud"), new IdentifierValue("t_user_detail"));
        assertThat(ownerToken.toString(), is("ud."));
        assertTokenGrid(ownerToken);
    }
    
    @Test
    public void assertOwnerTokenWithNoRouteUnitAndOwnerNameValueIsEmpty() {
        OwnerToken ownerToken = new OwnerToken(0, 1, new IdentifierValue(""), new IdentifierValue("t_user_detail"));
        assertThat(ownerToken.toString(), is(""));
        assertTokenGrid(ownerToken);
    }
    
    @Test
    public void assertOwnerTokenWithNoRouteUnitAndOwnerNameIsEmpty() {
        OwnerToken ownerToken = new OwnerToken(0, 1, null, new IdentifierValue("t_user_detail"));
        assertThat(ownerToken.toString(), is(""));
        assertTokenGrid(ownerToken);
    }
    
    private void assertTokenGrid(final OwnerToken ownerToken) {
        assertThat(ownerToken.getStartIndex(), is(0));
        assertThat(ownerToken.getStopIndex(), is(1));
    }
    
    private RouteUnit buildRouteUnit() {
        return new RouteUnit(new RouteMapper("logic_db", "logic_db"), Collections.singletonList(new RouteMapper("t_user", "t_user_0")));
    }
}
