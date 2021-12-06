package org.apache.shardingsphere.agent.core.bytebuddy.transformer.advice;

import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.ClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.ConstructorAdvice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ComposeConstructorAdviceTest {

    private ComposeConstructorAdvice composeConstructorAdvice;
    
    @Test
    public void onConstructorTest() {
        try {
            ConstructorAdvice advice = mock(ConstructorAdvice.class);
            AdviceTargetObject adviceTargetObject = mock(AdviceTargetObject.class);
            List<ConstructorAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
            composeConstructorAdvice = new ComposeConstructorAdvice(adviceList);
            composeConstructorAdvice.onConstructor(adviceTargetObject, new Object[2]);
        } catch (Exception e) {
            fail();
        }
    }
}