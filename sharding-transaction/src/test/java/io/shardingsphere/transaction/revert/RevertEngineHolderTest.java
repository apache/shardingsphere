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

package io.shardingsphere.transaction.revert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RevertEngineHolderTest {
    
    @Mock
    private RevertEngine mockRevertEngine;
    
    @Test
    public void assertGetRevertEngine() {
        RevertEngine revertEngine = RevertEngineHolder.getInstance().getRevertEngine();
        assertThat(revertEngine, instanceOf(EmptyRevertEngine.class));
        
        RevertEngineHolder.getInstance().setDefaultRevertEngine(mockRevertEngine);
        revertEngine = RevertEngineHolder.getInstance().getRevertEngine();
        assertThat(revertEngine, equalTo(mockRevertEngine));
    }
    
    @Test
    public void assertSetRevertEngineAndRemove() {
        RevertEngineHolder.getInstance().setDefaultRevertEngine(new EmptyRevertEngine());
        RevertEngine revertEngine = RevertEngineHolder.getInstance().getRevertEngine();
        assertThat(revertEngine, instanceOf(EmptyRevertEngine.class));
        
        RevertEngineHolder.getInstance().setRevertEngine(mockRevertEngine);
        revertEngine = RevertEngineHolder.getInstance().getRevertEngine();
        assertThat(revertEngine, equalTo(mockRevertEngine));
        
        RevertEngineHolder.getInstance().remove();
        revertEngine = RevertEngineHolder.getInstance().getRevertEngine();
        assertThat(revertEngine, instanceOf(EmptyRevertEngine.class));
    }
}
