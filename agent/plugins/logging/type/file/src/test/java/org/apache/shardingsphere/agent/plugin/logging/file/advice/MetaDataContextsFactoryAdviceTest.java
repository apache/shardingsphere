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

package org.apache.shardingsphere.agent.plugin.logging.file.advice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class MetaDataContextsFactoryAdviceTest {
    
    private ListAppender<ILoggingEvent> listAppender;
    
    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(MetaDataContextsFactoryAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }
    
    @Test
    void assertLog() {
        MetaDataContextsFactoryAdvice advice = new MetaDataContextsFactoryAdvice();
        Method method = mock(Method.class);
        advice.beforeMethod(null, method, new Object[]{}, "FIXTURE");
        advice.afterMethod(null, method, new Object[]{}, null, "FIXTURE");
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.size(), equalTo(1));
        assertThat(logsList.get(0).getMessage(), is("Build meta data contexts finished, cost {} milliseconds."));
        assertThat(logsList.get(0).getLevel(), is(Level.INFO));
    }
}
