package com.edu.bupt.new_account.service.impl;

import com.edu.bupt.new_account.dao.RelationMapper;
import com.edu.bupt.new_account.dao.TenantMapper;
import com.edu.bupt.new_account.dao.UserMapper;
import com.edu.bupt.new_account.model.Relation;
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

    @Autowired
    private RelationMapper relationMapper;

    @Override
    public Tenant findTenantByNameAndPasswd(String tenantName, String passwd) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setPassword(passwd);
        return tenantMapper.selectByNameAndPassword(tenant);
    }

    @Override
    public Integer saveUser(User user) {
        return userMapper.insert(user);
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

    @Override
    public User findUserByphone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public void saveRelation(Relation relation) {
        relationMapper.insert(relation);
    }

    @Override
    public List<Relation> getBindedRelations(int bindedId) {
        return relationMapper.getBindedRelations(bindedId);
    }

    @Override
    public Relation findRelationByBinderAndBinded(int binderId, int bindedId) {
        Relation re = new Relation();
        re.setBinder(binderId);
        re.setBinded(bindedId);
        return relationMapper.getRelationBy2Bind(re);
    }

    @Override
    public void unbind(Integer id) {
        relationMapper.deleteByPrimaryKey(id);
    }



    @Override
    public User findUserById(Integer binded) {
        return userMapper.selectByPrimaryKey(binded);
    }

    @Override
    public List<Relation> findRelationsByBinderID(int binderId) {
        return relationMapper.getRelationsByBinderId(binderId);
    }

    @Override
    public void updateRelation(Relation re) {
        relationMapper.updateByPrimaryKey(re);
    }

    public void deleteUserById(Integer id){
        userMapper.deleteById(id);
    }

    public Boolean is_shared(String old_gatewayids, String new_gatewayids){
        String [] gatewayids = new_gatewayids.split(",");
        for (String gatewayid: gatewayids){
            if (old_gatewayids.contains(gatewayid)){
                return true;
            }
        }
        return false;
    }


}
