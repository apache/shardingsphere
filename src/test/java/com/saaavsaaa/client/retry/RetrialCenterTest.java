package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.utility.retry.RetrialCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by aaa
 */
public class RetrialCenterTest {
    @Before
    public void start(){
        RetrialCenter.INSTANCE.start();
    }
    
    @After
    public void stop(){
        
    }
    
    @Ignore
    @Test
    public void nothing(){
        
    }
}
