package org.shardingsphere.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.shardingsphere.domain.User;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * insert a lone of data
     *
     * @param record message of user
     * @author: Jieker
     * @date: 2020-12-31  15:14
     **/
    int insert(User record);

    /**
     * select all of data
     *
     * @author: Jieker
     * @date: 2020-12-31  15:14
     **/
    List<User> showAll();

    /**
     * delete a user
     *
     * @param id userId
     * @return: void
     * @author: Jieker
     * @date: 2020-12-31  15:25
     **/
    void deleteUser(Long id);
}