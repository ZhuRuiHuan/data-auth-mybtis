package com.xuanyue.mybatisplug.label;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.*;

public class DataLabel {

    public DataLabel ( String dataLabel ){
        this.dataLabel = dataLabel;
        this.dataStatement = format( dataLabel );
    }

    // label标签 , 格式样例 @{ value } , value 是 Ognl 表达式
    private String dataLabel;

    // Ognl 表达式
    private String dataStatement;

    /**
     * 获取数据标签
     */
    public String getDataLabel() {
        return dataLabel;
    }

    /**
     * 获取 取值Ognl表达式
     * @param dataLabel
     * @return
     */
    private String format( String dataLabel ){
        return dataLabel.replace("@","")
                        .replace("{","")
                        .replace("}","");
    }

    /**
     * 获取label的值
     * @param contentMap
     * @return
     */
    public String getValueString(Map contentMap){
        Object value = null;
        try {
            value = Ognl.getValue( dataStatement , contentMap );
        } catch (OgnlException e) {
            e.printStackTrace();
        }
        return valueToString(value);
    }

    /**
     * 获取字符串值
     * @param value
     * @return
     */
    public static String valueToString( Object value ) {

        // 值为空,且是集合类型,返回一般情况下无法匹配的到的参数 'size is 0'
        if ( value == null && value instanceof Collection ){
            return "('size is 0')";
        }
        // 值为空 返回 null ,一般运算符能处理
        else if ( value == null ) {
            return "null";
        }

        // 获取类型
        String type = value.getClass().getName();

        // 字符类型,返回要加上""
        if ("java.lang.String".equals(type)) {
            return "\"" + value + "\"";
        }
        // 基础类型,转为String类型返回
        else if ("java.lang.Integer" .equals(type) || "int"   .equals(type) ||
                 "java.lang.Short"   .equals(type) || "short" .equals(type) ||
                 "java.lang.Float"   .equals(type) || "float" .equals(type) ||
                 "java.lang.Double"  .equals(type) || "double".equals(type) ||
                 "java.lang.Long"    .equals(type) || "long"  .equals(type)) {

            return String.valueOf(value);
        }
        // 布尔类型,改为 0 1
        else if( "java.lang.Boolean" .equals(type) || "boolean".equals(type) ){
            Boolean var =  (Boolean)value;
            if( var )
                return "1";
            else
                return "0";
        }
        // 集合类型,遍历返回
        else if ( value instanceof Collection ) {

            Collection collection = (Collection) value;

            if (collection.size() == 0) {
                return "('size is 0')";
            }

            StringBuffer sb = new StringBuffer("(");

            for (Object obj : collection) {
                sb.append(valueToString(obj)).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");

            return sb.toString();
        }
        // 时间类型,没想好怎么处理,也很少用时间类型做数据权限,所以暂时不写了
        else if( "java.util.Date".equals( type ) ){
            throw new RuntimeException("无法处理Date类型参数");
        }
        // 其他类型,抛出异常,冷门类型,可以自己接着写
        else {
            throw new RuntimeException("无法处理的类型:" + type);
        }
    }

}
