package com.xuanyue.mybatisplug.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.xuanyue.mybatisplug.annotation.AuthData;
import com.xuanyue.mybatisplug.auth.IAuthData;
import com.xuanyue.mybatisplug.label.SqlHandler;

/**
 * 	控制类<br>
 * 	保存IAuthData所有实例<br>
 * 	<br>
 * 
 * @author Administrator
 *
 */
public class DataAuthHandler {
	
	// 数据权限分组实例
	private Map<String,IAuthData> dataTypeMap = new HashMap<>();

	// sql缓存,服务启动后,所有调用过的SQL都会在这里缓存
	private Map<String,SqlHandler> sqlHandlerMap = new HashMap<>();


	/**
	 * 	初始化<br>
	 * 	因为如果在IDataAuth中注入Mybatis-Dao层的实例<br>
	 * 	可能会出现Bean加载顺序的问题<br>
	 * 	因此会在所有Bean加载完成后调用init方法进行初始化<br>
	 * @param dataAuthMap ( key = 拦截器名称 , value = IDataAuth实例 )
	 */
	public void init ( Map<String,IAuthData> dataAuthMap ) {
		this.dataTypeMap = dataAuthMap;
	}


	/**
	 * 解析sql
	 * 1.寻找sqlId对应的SqlHandler
	 * 2.判断该sqlId对应的方法是否加入数据权限控制
	 * 3.如果有权限控制,就进权限控制取值操作
	 *
	 * @param targetSql
	 * @param sqlId
	 * @return
	 */
	public String parseSql( String targetSql , String sqlId ) {

		// 尝试从缓存中获取SqlHandler信息
		SqlHandler sqlHandler = sqlHandlerMap.get( sqlId );

		// 初始化SqlHandler信息
		if( sqlHandler == null )
			sqlHandler = findSqlHandler( sqlId , targetSql );

		// 判断是否是有@AuthData
		if( !sqlHandler.isAuthSql() )
			return targetSql;

		// 取值操作
		String result = sqlHandler.setAuthValue( targetSql );

		return result;
	}

	/**
	 * 	根据查询ID找到缓存<br>
	 * 	如果有找到,则创建
	 * @return
	 */
	public SqlHandler findSqlHandler( String sqlId , String sql ) {

		/*
		 * 	 命中缓存,直接返回
		 */
		SqlHandler result = sqlHandlerMap.get( sqlId );
		if ( result != null ) return result;

		/*
		 *	否则将queryId视作mapper方法的全路径名称来处理
		 * 	确认方法是否包含@AuthData注释
		 */

		int slipt = sqlId.lastIndexOf(".");

		String className = sqlId.substring(0, slipt);
		String methodName = sqlId.substring( slipt + 1 );
		Class<?> clazz;
		try {

			clazz = Class.forName( className );

		} catch (ClassNotFoundException e) {

			// 根据 queryId 找不到class,加入false
			result = new SqlHandler( sqlId , false );
			sqlHandlerMap.put( sqlId, result );
			e.printStackTrace();
			return result;

		}

		// 找到方法中的@DataAuth,取出authName
		Method[] methods = clazz.getMethods();

		String[] authTypes = null;

		for ( Method method : methods ) {
			if (methodName.equals(method.getName()) && method.isAnnotationPresent(AuthData.class)) {

				AuthData dataAuth = method.getAnnotation(AuthData.class);

				authTypes = dataAuth.value();

				List<IAuthData> authDataList = new LinkedList<>();

				if (authTypes == null || authTypes.length == 0) {

					authDataList.addAll(dataTypeMap.values());

				} else {

					for (String authType : authTypes) {
						authDataList.add(dataTypeMap.get(authType));
					}

				}

				result = new SqlHandler( sqlId , true , sql , authDataList );

				break;

			}
		}

		if( result == null ){
			result = new SqlHandler( sqlId , false );
		}

		sqlHandlerMap.put( sqlId, result );
		return result;
	}
	
}
