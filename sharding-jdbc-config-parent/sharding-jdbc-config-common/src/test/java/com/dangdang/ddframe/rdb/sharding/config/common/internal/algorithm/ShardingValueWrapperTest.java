/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ShardingValueWrapperTest {
    
    private static final String formatText = "yyyy-MM-dd";
    
    private static final String dateText = "2016-02-13";
    
    private static final Date now = new Date();
    
    private static SimpleDateFormat format;
    
    private static Date date;
    
    @BeforeClass
    public static void init() throws ParseException {
        format = new SimpleDateFormat(formatText);
        date = format.parse(dateText);
    }
    
    @Test
    public void testLongValue() throws Exception {
        assertThat(new ShardingValueWrapper((short) 1).longValue(), is(1L));
        assertThat(new ShardingValueWrapper(1).longValue(), is(1L));
        assertThat(new ShardingValueWrapper(1L).longValue(), is(1L));
        assertThat(new ShardingValueWrapper(1.0F).longValue(), is(1L));
        assertThat(new ShardingValueWrapper(1.0D).longValue(), is(1L));
        assertThat(new ShardingValueWrapper("1").longValue(), is(1L));
        Date now = new Date();
        assertThat(new ShardingValueWrapper(now).longValue(), is(now.getTime()));
    }
    
    @Test
    public void testDoubleValue() throws Exception {
        assertThat(new ShardingValueWrapper((short) 1).doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper(1).doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper(1L).doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper(1.0F).doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper(1.0D).doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper("1").doubleValue(), is(1.0D));
        assertThat(new ShardingValueWrapper(now).doubleValue(), is((double) now.getTime()));
    }
    
    @Test
    public void testDateValue() throws Exception {
        Date now = new Date();
        assertThat(new ShardingValueWrapper(now).dateValue(), is(now));
        assertThat(new ShardingValueWrapper(now.getTime()).dateValue(), is(now));
        assertThat(new ShardingValueWrapper(format.format(date)).dateValue(formatText), is(date));
    }
    
    @Test
    public void testString() throws Exception {
        assertThat(new ShardingValueWrapper((short) 1).toString(), is("1"));
        assertThat(new ShardingValueWrapper(1).toString(), is("1"));
        assertThat(new ShardingValueWrapper(1L).toString(), is("1"));
        assertThat(new ShardingValueWrapper(1.0F).toString(), is("1.0"));
        assertThat(new ShardingValueWrapper(1.0D).toString(), is("1.0"));
        assertThat(new ShardingValueWrapper("1").toString(), is("1"));
        assertThat(new ShardingValueWrapper(now).toString(), is(now.toString()));
        assertThat(new ShardingValueWrapper(date).toString(formatText), is(dateText));
        assertThat(new ShardingValueWrapper(date.getTime()).toString(formatText), is(dateText));
        assertThat(new ShardingValueWrapper(dateText).toString(formatText), is(dateText));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedNumber() throws Exception {
        new ShardingValueWrapper(true).longValue();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedDate() throws Exception {
        new ShardingValueWrapper(true).dateValue();
    }
}
