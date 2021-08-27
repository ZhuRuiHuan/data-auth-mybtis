package com.xuanyue.mybatisplug.auth;

import java.util.Map;

/**
 * 权限数据
 * 设想在极端情况下,一个系统可能会在无数个地方获取权限数据
 * 而每个地方获取权限数据开销巨大,如果每次调用都调用所有的
 * IAuthData.getDataAuth()获取数据,难以承受
 * 所以可以使用getAuthType进行分组
 * @AuthData( {"typeName"} )可以获取指定的权限数据
 */
public interface IAuthData {

    /**
     * 数据类型,可以将一个IAuthData视为一个组
     * @return
     */
    String getAuthType();

    /**
     * 返回在权限控制语句中需要用到的数据
     * @return
     */
    Map<String,Object> getDataAuth();

}
