package com.xuanyue.mybatisplug;

import java.util.HashMap;
import java.util.Map;

import com.xuanyue.mybatisplug.auth.IAuthData;
import com.xuanyue.mybatisplug.handler.DataAuthHandler;
import com.xuanyue.mybatisplug.plug.DataAuthPlug;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class AutoConfigure implements ApplicationContextAware , ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired

	private static ApplicationContext applicationContext ;

	/**
	 *	 装配拦截器
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(DataAuthPlug.class)
	public DataAuthPlug dataAuthPlug() {
		return new DataAuthPlug();
	}
	
	/**
	 * 	装配数据权限控制器
	 * @param
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(DataAuthHandler.class)
	public DataAuthHandler dataAuthHandler( ) {
		return new DataAuthHandler( );
	}

	/**
	 *	获取spring上下文
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		AutoConfigure.applicationContext = applicationContext;
	}
	
	/**
	 * 初始化DataAuthHandler
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		DataAuthHandler dataAuthHandler = applicationContext.getBean(DataAuthHandler.class);
		Map<String, IAuthData> beanMap = applicationContext.getBeansOfType(IAuthData.class);
		Map<String,IAuthData> dataAuthMap = new HashMap<>(beanMap.size());
		
		beanMap.forEach( (k,v) -> dataAuthMap.put( v.getAuthType() , v )  );
		
		dataAuthHandler.init(dataAuthMap);
		
	}
}
