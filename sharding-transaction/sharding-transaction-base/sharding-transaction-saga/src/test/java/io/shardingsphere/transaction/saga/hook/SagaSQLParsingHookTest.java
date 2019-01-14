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

package io.shardingsphere.transaction.saga.hook;

import io.shardingsphere.transaction.saga.SagaTransaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SagaSQLParsingHookTest {
    
    @Mock
    private SagaTransaction sagaTransaction;
    
    private final SagaSQLParsingHook sagaSQLParsingHook = new SagaSQLParsingHook();
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field sagaTransactionField = SagaSQLParsingHook.class.getDeclaredField("sagaTransaction");
        sagaTransactionField.setAccessible(true);
        sagaTransactionField.set(sagaSQLParsingHook, sagaTransaction);
    }
    
    @Test
    public void assertFinishSuccess() {
        sagaSQLParsingHook.finishSuccess();
        verify(sagaTransaction).nextLogicSQL();
    }
}