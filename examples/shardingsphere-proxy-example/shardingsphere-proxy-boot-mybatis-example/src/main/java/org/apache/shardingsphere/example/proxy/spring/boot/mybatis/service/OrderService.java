package org.apache.shardingsphere.example.proxy.spring.boot.mybatis.service;

import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.entity.Address;
import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.entity.Order;
import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.entity.OrderItem;
import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.repository.AddressRepository;
import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.repository.OrderItemRepository;
import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
    
    @Resource
    private AddressRepository addressRepository;
    
    public void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        initAddressTable();
    }
    
    private void initAddressTable() throws SQLException {
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
        for (int i = 1; i <= 10; i++) {
            Address entity = new Address();
            entity.setAddressId((long) i);
            entity.setAddressName("address_" + i);
            addressRepository.insert(entity);
        }
    }
    
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
    
    @Transactional
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData();
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    @Transactional
    public void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }
    
    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(i);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
    }
    
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        }
    }
}