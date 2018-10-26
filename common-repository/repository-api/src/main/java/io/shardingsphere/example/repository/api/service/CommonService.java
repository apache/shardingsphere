package io.shardingsphere.example.repository.api.service;

public interface CommonService {
    
    void initEnvironment();
    
    void cleanEnvironment();
    
    void processSuccess(boolean isRangeSharding);
    
    void processFailure();
    
    void printData();
}
