package com.edu.bupt.new_account.service;

import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;

public interface UserService {

    void saveUser(User user);

    Tenant findTenantByNameAndPasswd(String tenantName, String passwd);
}
