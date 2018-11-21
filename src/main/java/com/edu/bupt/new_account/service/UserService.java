package com.edu.bupt.new_account.service;

import com.edu.bupt.new_account.model.Relation;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;

import java.util.List;

public interface UserService {

    Tenant findTenantByNameAndPasswd(String tenantName, String passwd);

    void saveUser(User user);

    User findUserByOpenid(String openid);

    void updateUserInfo(User user);

    List<User> findAllUser();

    User findUserByphone(String phone);

    void saveRelation(Relation relation);

    List<Relation> getBindedRelations(int bindedId);

    Relation findRelationByBinderAndBinded(int binderId, int bindedId);

    void unbind(Integer id);


    User findUserById(Integer binded);

    List<Relation> findRelationsByBinderID(int binderId);
}
