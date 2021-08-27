package com.xuanyue.mybatisplug.plug;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;

import com.xuanyue.mybatisplug.handler.DataAuthHandler;

@Intercepts
({
	@Signature( type = Executor.class,
				 method = "query",
				 args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
			   ),
	@Signature( type = Executor.class,
				method = "update",
				args = {MappedStatement.class, Object.class}
				)
})


public class DataAuthPlug implements Interceptor {
	
	@Autowired
	private DataAuthHandler dataAuthHandler;
	
	 @Override
	    public Object intercept(Invocation invocation) throws Throwable {

	        final Object[] queryArgs = invocation.getArgs();
	        final MappedStatement mappedStatement = (MappedStatement) queryArgs[0];
	        final BoundSql boundSql = mappedStatement.getBoundSql(queryArgs[1]);

	        String queryId = mappedStatement.getId();
	        
	        String sql = boundSql.getSql();
	        
	        String authSql = dataAuthHandler.parseSql(sql, queryId);
	        
	        
	        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), authSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
	       
	        MappedStatement newMs = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql));
	        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
	            String prop = mapping.getProperty();
	            if (boundSql.hasAdditionalParameter(prop)) {
	                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
	            }
	        }
	        queryArgs[0] = newMs;
	        return invocation.proceed();
	    }

	    @Override
	    public Object plugin(Object target) {
	        return Plugin.wrap(target, this);
	    }
	 
	    @Override
	    public void setProperties(Properties properties) {
	 
	    }
	 
	    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
	        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
	        builder.resource(ms.getResource());
	        builder.fetchSize(ms.getFetchSize());
	        builder.statementType(ms.getStatementType());
	        builder.keyGenerator(ms.getKeyGenerator());
	        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
	            builder.keyProperty(ms.getKeyProperties()[0]);
	        }
	        builder.timeout(ms.getTimeout());
	        builder.parameterMap(ms.getParameterMap());
	        builder.resultMaps(ms.getResultMaps());
	        builder.resultSetType(ms.getResultSetType());
	        builder.cache(ms.getCache());
	        builder.flushCacheRequired(ms.isFlushCacheRequired());
	        builder.useCache(ms.isUseCache());
	        return builder.build();
	    }
	 
	    public static class BoundSqlSqlSource implements SqlSource {
	        private BoundSql boundSql;
	        public BoundSqlSqlSource(BoundSql boundSql) {
	            this.boundSql = boundSql;
	        }
	        public BoundSql getBoundSql(Object parameterObject) {
	            return boundSql;
	        }
	    }

}
