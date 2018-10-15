package io.shardingsphere.example.repository.api.service;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.api.repository.OrderItemRepository;
import io.shardingsphere.example.repository.api.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonServiceImpl implements CommonService {
    
    @Override
    public void initEnvironment() {
        getOrderRepository().createTableIfNotExists();
        getOrderItemRepository().createTableIfNotExists();
        getOrderRepository().truncateTable();
        getOrderItemRepository().truncateTable();
    }
    
    @Override
    public void cleanEnvironment() {
        getOrderItemRepository().dropTable();
        getOrderItemRepository().dropTable();
    }
    
    @Override
    public void processSuccess() {
        List<Long> orderIds = insertData();
        printData();
        deleteData(orderIds);
        printData();
    }
    
    @Override
    public void processFailure() {
    }
    
    private List<Long> insertData() {
        System.out.println("1.Insert--------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            Order order = newOrder();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            getOrderRepository().insert(order);
            OrderItem item = newOrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            getOrderItemRepository().insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) {
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            getOrderRepository().delete(each);
            getOrderItemRepository().delete(each);
        }
    }
    
    private void printData() {
        System.out.println("Order Data--------------");
        System.out.println(getOrderRepository().selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(getOrderItemRepository().selectAll());
    }
    
    protected abstract OrderRepository getOrderRepository();
    
    protected abstract OrderItemRepository getOrderItemRepository();
    
    protected abstract Order newOrder();
    
    protected abstract OrderItem newOrderItem();
}
