# mybatis数据权限插件

#### 介绍


#### 软件架构
#### 软件架构说明
#### spring-boot-starter mybatis

#### 使用方法

#### 1.下载项目,打成jar包,或者直接下载jar包
#### 2.在spring-boot中引入
#### 3.实现 IAuthData 接口并注册到spring容器中, 其中getAuthType()为分组名, getDataAuth()为解析权限控制语句时,获取权限数据的方法
#### 4.在 mybatis.Dao的接口方法上,加上@AuthData,可以以数组方式传入数据分组名称,如果不传则调用所有IAuthData实例的getDataAuth()方法获取数据
#### 5.在对应的sql标签里,加入权限控制语句:
#####例如:
    getDataAuth() 返回: 
        { "userType": 1 , "orgList": [ "001" , "002" ] , "areaCode" : 1001 , "bol": true }
    
    在sql中写下: 
        #( userType == 1 )[ AND org_id IN @{ orgList } ]
        
    最终会解析为: 
        AND org_id IN ("001","002")
    
    #[ AND status = @{ bol } ] 解释为 AND status = 1
    
#### 规则
##### 标签以#开头,后面衔接"()"或者"[]"
##### "()"类似if标签,内容为ognl表达式,最终结果应为boolean类型;可以省略,则默认有效
##### "[]"包含任意内容,可以包含@{}取值标签
##### @{} ognl表达式,结果支持基本类型,String,Collection集合;boolean类型会转为1和0,集合类型会转为(value1,value2....)
#### ognl表达式,就是mybatis,if-test里用的
