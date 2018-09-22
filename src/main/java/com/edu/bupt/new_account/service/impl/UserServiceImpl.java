package com.edu.bupt.new_account.service.impl;

import com.edu.bupt.new_account.dao.TenantMapper;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TenantMapper tenantMapper;

    @Override
    public Tenant findTenantByNameAndPasswd(String tenantName, String passwd) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setPassword(passwd);
        return tenantMapper.selectByNameAndPassword(tenant);
    }


}
