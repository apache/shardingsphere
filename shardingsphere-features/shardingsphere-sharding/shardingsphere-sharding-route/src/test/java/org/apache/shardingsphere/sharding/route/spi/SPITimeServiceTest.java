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

package org.apache.shardingsphere.sharding.route.spi;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sharding.route.fixture.TimeServiceFixture;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SPITimeServiceTest {
    
    private final SPITimeService timeService = new SPITimeService();
    
    @Test
    public void assertGetTime() {
        Optional<TimeServiceFixture> optional = getFixtureHook();
        assertTrue(optional.isPresent());
        Date date = new Date();
        optional.get().setDate(date);
        Date time = timeService.getTime();
        assertThat(date, is(time));
    }
    
    @Test
    public void assertGetTimeWithDefault() {
        Optional<TimeServiceFixture> optional = getFixtureHook();
        assertTrue(optional.isPresent());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = calendar.getTime();
        Date time = timeService.getTime();
        assertTrue(time.after(date));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<TimeServiceFixture> getFixtureHook() {
        Field routingHooksField = SPITimeService.class.getDeclaredField("timeServices");
        routingHooksField.setAccessible(true);
        Collection<TimeService> timeServices = (Collection<TimeService>) routingHooksField.get(timeService);
        for (TimeService timeService : timeServices) {
            if (timeService instanceof TimeServiceFixture) {
                return Optional.of((TimeServiceFixture) timeService);
            }
        }
        return Optional.empty();
    }
}
