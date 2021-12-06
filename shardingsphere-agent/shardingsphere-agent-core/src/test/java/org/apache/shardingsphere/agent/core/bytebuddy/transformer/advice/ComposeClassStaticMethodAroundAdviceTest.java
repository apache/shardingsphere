package org.apache.shardingsphere.agent.core.bytebuddy.transformer.advice;

import org.apache.shardingsphere.agent.api.advice.ClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ComposeClassStaticMethodAroundAdviceTest {

    private ComposeClassStaticMethodAroundAdvice composeClassStaticMethodAroundAdvice;
    
    @Test
    public void beforeMethodTest() {
        try {
            ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
            Method method = mock(Method.class);
            MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
            List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
            composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
            composeClassStaticMethodAroundAdvice.beforeMethod(String.class, method, new Object[2], methodInvocationResult);
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void afterMethodTest() {
        try {
            ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
            Method method = mock(Method.class);
            MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
            List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
            composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
            composeClassStaticMethodAroundAdvice.afterMethod(String.class, method, new Object[2], methodInvocationResult);
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void onThrowingTest() {
        try {
            ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
            Method method = mock(Method.class);
            List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
            composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
            composeClassStaticMethodAroundAdvice.onThrowing(String.class, method, new Object[2], new NullPointerException("Null Pointer"));
        } catch (Exception e) {
            fail();
        }
    }
}
