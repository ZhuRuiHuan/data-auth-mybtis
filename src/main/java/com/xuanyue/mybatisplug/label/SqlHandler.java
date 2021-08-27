package com.xuanyue.mybatisplug.label;


import com.xuanyue.mybatisplug.auth.IAuthData;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SqlHandler {

    // sqlId
    private String sqlId;

    // 是否包含权限控制注解
    private boolean isAuthSql;

    // 权限数据获取列表
    private List<IAuthData> authDataList;

    // 解析出的权限控制语句
    private List<AuthLabel> labelList;

    public SqlHandler ( String sqlId , boolean isAuthSql ){
        this.sqlId = sqlId;
        this.isAuthSql = isAuthSql;
    }

    public SqlHandler ( String sqlId , boolean isAuthSql , String sql , List<IAuthData> authDataList ){

        this.sqlId = sqlId;

        this.isAuthSql = isAuthSql;

        this.authDataList = authDataList;

        if( isAuthSql == true && sql != null && sql != "" ){
            this.labelList = AuthLabel.formatSql( sql );
        }

    }

    /**
     * 获取权限值
     * 1.从@AuthDatab中指定的数据组获取数据
     * 2.处理sql中的authLabel
     * @param sql
     * @return
     */
    public String setAuthValue( String sql ){

        Map<String,Object> authValue = new HashMap<>();

        for (IAuthData iAuthData : authDataList) {
            Map<String,Object> value = iAuthData.getDataAuth();
            if( value != null ) authValue.putAll( value );
        }

        for (AuthLabel authLabel : labelList) {
            // 将处理的结果返回给下一个label接着处理
            sql = authLabel.parseAuthLabel( authValue , sql );
        }

        return sql;
    }

    public boolean isAuthSql() {
        return isAuthSql;
    }


    public static void main(String[] args) throws OgnlException {

        Map<String,Object> map = new HashMap<>();

        map.put( "a" , true );
        map .put( "b" , "abc" );

        List<String> list = new LinkedList<>();
        list.add( "a" );
        list.add( "ab" );
        list.add( "abc" );
        list.add( "abcd" );
        list.add( "abcde" );
        list.add( "abcdef" );
        list.add( "abcdefg" );
        map.put( "list" , list );
        System.out.println(Ognl.getValue(  " 'abc'.equals(1) " , map));


//        String sql = " select 1 from dual where 1=1 # ( true )[ AND vehicleNo  = @{ a } ] " +
//                        "# [ AND muc_id = @{ a } AND area_code IN @{ list } ] ";


    }

}
