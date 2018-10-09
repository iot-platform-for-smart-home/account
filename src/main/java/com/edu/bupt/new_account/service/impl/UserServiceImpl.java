package com.edu.bupt.new_account.service.impl;

import com.edu.bupt.new_account.dao.TenantMapper;
import com.edu.bupt.new_account.dao.UserMapper;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;
import com.edu.bupt.new_account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Tenant findTenantByNameAndPasswd(String tenantName, String passwd) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setPassword(passwd);
        return tenantMapper.selectByNameAndPassword(tenant);
    }

    @Override
    public void saveUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public User findUserByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    public void updateUserInfo(User user) {
        userMapper.updateByUser(user);
    }

    @Override
    public List<User> findAllUser() {
        return userMapper.searchAllUser();
    }


}
