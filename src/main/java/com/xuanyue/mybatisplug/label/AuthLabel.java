package com.xuanyue.mybatisplug.label;


import ognl.Ognl;
import ognl.OgnlException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AuthLabel {
    
    /*
     * 权限控制语句,样式:
     * #( 判断,Ognl表达式 )[ 任意内容 | @{ 取值,Ognl表达式 } | 任意内容 ]
     * #[ 任意内容 | @{ 取值,Ognl表达式 } | 任意内容 ]
     */
    private String label;

    // () 内的判断 Ognl表达式
    private String judgmentStatement;

    // [] 内的权限语句
    private String authStatement;

    // 控制语句中的 取值标签
    private List<DataLabel> dataLabelList;

    public AuthLabel(String label, String judgmentStatement, String authStatement) {
        this.label = label;
        this.judgmentStatement = judgmentStatement;
        this.authStatement = authStatement;
        this.dataLabelList = formatAuthStatement( authStatement );
    }

    /**
     * 解析label
     * @param authValue
     * @param sql
     * @return
     */
    public String parseAuthLabel( Map<String,Object> authValue , String sql ){

        // 判断label本次是否生效
        boolean isValid = isExecute( authValue , judgmentStatement );

        // 如果不生效,将label置换为空
        if( isValid == false )
            return sql.replace( label , "" );

        /**
         * 取值操作
         * 1.从DataLabel中获取值,
         * 2.然后将authLabel中的数据标签替换为最终值,
         * 3.最后将sql中的authLabel替换为处理过后的label结果
         */
        String result = authStatement;

        for (DataLabel dataLabel : dataLabelList) {
            // 从dataLabel中取值
            String var = dataLabel.getValueString( authValue );
            result = result.replace( dataLabel.getDataLabel() , var );
        }

        // 替换authLabel
        return sql.replace( label , result );
    }

    /**
     * 判断权限语句是否生效
     * 如果没有 () 判断语句,默认生效
     * @param authValue 权限参数
     * @param judgmentStatement 判断语句
     * @return
     */
    private boolean isExecute( Map<String,Object> authValue , String judgmentStatement ){

        if( judgmentStatement == null ){
            return true;
        }
        else {

            Object judgment;

            try {
                // 执行判断条件的Ognl表达式
                judgment = Ognl.getValue( judgmentStatement , authValue );
            } catch ( OgnlException e) {
                e.printStackTrace();
                throw new RuntimeException("数据权限控制解析错误=>表达式:"+judgmentStatement+";取值错误:\n");
            }

            // 这里要求返回值是boolean类型
            String judgmentType = judgment.getClass().getName();

            if( !"java.lang.Boolean" .equals(judgmentType) && !"boolean".equals(judgmentType) ){
                throw new RuntimeException("数据权限控制解析错误=> 表达式:" + judgmentStatement +";的结果不是boolean类型" );
            }
            return (Boolean) judgment;
        }
    }

    /**
     * 从控制语句中找到取值标签
     * @param authStatement
     * @return
     */
    private static List<DataLabel> formatAuthStatement( String authStatement ){

        List<DataLabel> result = new LinkedList<>();

        char[] chars = authStatement.toCharArray();

        int index = -1;
        int inn = -1;
        int innIndex = -1;

        for( int i = 0; i < chars.length; i++ ){

            char cr = chars[i];

            // 第一阶段,定位 @
            if( index == -1 ){
                if( cr == '@' ){
                    index = i;
                }
                continue;
            }

            // 第二阶段,定位 {
            if( index != -1 && inn == -1 ){
                // 遇到非空字符,出栈
                if( cr > 32 && cr != '{' ){
                    index = -1;
                }
                if( cr == '{' ){
                    inn = 1;
                    innIndex = i;
                }
                continue;
            }
            // 第三阶段 走完{}数据区
            if( index != -1 && inn > 0 ){
                if( cr == '{' ){
                    inn++;
                }
                if( cr == '}' ){
                    inn--;
                }
                if( inn == 0 ){
                    String authData = authStatement.substring( index , i + 1 );
                    DataLabel dataLabel = new DataLabel( authData );
                    result.add( dataLabel );

                    index = -1;
                    inn = -1;
                    innIndex = -1;
                }
            }

        }

        return result;
    }

    /**
     * 读取sql中的权限控制语句
     * @param sql
     * @return
     */
    public static List<AuthLabel> formatSql( String sql ){

        List<AuthLabel> result = new LinkedList<>();

        char[] chars = sql.toCharArray();

        // '#' 计数器
        int inn0 = -1;
        // '(' 计数器
        int inn1 = -1;
        int inn1Index = -1;
        String inn1Content = null;
        // '[' 计数器
        int inn2 = -1;
        int inn2Index = -1;
        String inn2Content = null;

        for( int i = 0; i < chars.length; i++ ){
            char cr = chars[i];

            // 第一阶段,找 '#'
            if( inn0 == -1 ){
                if( cr == '#' ){
                    inn0 = i;
                }
                continue;
            }

            // 第二阶段 没有 '( '和 '[' 入栈的情况下 ,如果有非空白字符,'#'计数器归零,跳回第一阶段
            if( inn0 != -1 && inn1 == -1 && inn2 == -1 ){
                // 非标准格式,复位
                if( cr > 32 &&  cr != '(' && cr != '[' ){
                    inn0 = -1;
                    continue;
                }
                // 找到 ( ,跳到第三阶段,
                if( cr == '(' ){
                    inn1 = 1;
                    inn1Index = i;
                    continue;
                }
                // 找到 [ ,跳至第五阶段
                if( cr == '[' ){
                    inn1 = 0;
                    inn2 = 1;
                    inn2Index = i;
                    continue;
                }
                continue;
            }
            // 第三阶段, 对'()'内容的指针移动,此时允许任意内容
            if( inn1 > 0 ){
                if( cr == '(' ){
                    inn1 ++;
                    continue;
                }
                if( cr == ')' ){
                    inn1 --;
                    if( inn1 == 0 ){
                        inn1Content = sql.substring( inn1Index + 1 , i );
                    }
                    continue;
                }
                continue;
            }
            // 第四阶段 , 完成'()'内容的指针移动,寻找 '[' ,此时同样只允许空白字符
            if( inn0 != -1 && inn1 == 0 && inn2 == -1 ){
                // 非标准格式,复位
                if( cr > 32 &&  cr != '[' ){
                    inn0 = -1;
                    inn1 = -1;
                    inn1Index = -1;
                    inn1Content = null;
                    continue;
                }
                if( cr == '[' ){
                    inn2 = 1;
                    inn2Index = i;
                    continue;
                }
                continue;
            }
            // 第五阶段,完成'[]'内容的指针移动,允许任意字符
            if( inn0 != -1 && inn1 == 0 && inn2 > 0 ){
                if( cr == '[' ){
                    inn2 ++;
                    continue;
                }
                if( cr == ']' ){
                    inn2 --;
                    if( inn2 == 0 ){
                        inn2Content = sql.substring( inn2Index + 1 , i );
                    }
                }
                // '[]' 内容完成归栈,读取完成,复位
                if( inn0 != -1 && inn1 == 0 && inn2 == 0 ){
                    String authLabel = sql.substring( inn0 , i +1 );
                    AuthLabel label = new AuthLabel( authLabel , inn1Content , inn2Content);
                    result.add(label);
                    // 所有指针复位
                    inn0 = -1;
                    inn1 = -1;
                    inn1Index = -1;
                    inn1Content = null;
                    inn2 = -1;
                    inn2Index = -1;
                    inn2Content = null;
                    continue;
                }
                continue;
            }
        }

        return result;

    }

}
