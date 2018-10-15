package io.shardingsphere.example.repository.api.service;

public interface CommonService {
    
    void initEnvironment();
    
    void cleanEnvironment();
    
    void processSuccess();
    
    void processFailure();
    
    void printData();
}
