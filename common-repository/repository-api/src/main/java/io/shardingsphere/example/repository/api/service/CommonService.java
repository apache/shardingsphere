package io.shardingsphere.example.repository.api.service;

import java.util.List;

public interface CommonService {
    
    void initTables();
    
    void printData();
    
    List<Long> insertData();
    
    void deleteData(final List<Long> idList);
    
    void cleanTables();
}
