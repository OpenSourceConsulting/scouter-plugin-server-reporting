package scouter.plugin.server.reporting.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public abstract class AbstractService {

	private static SqlSessionFactory sqlSessionFactory;
	private static SqlSession session;
	
	private synchronized void initSession() {
		if (sqlSessionFactory == null) {
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(AbstractService.class.getResourceAsStream("/mybatis-config.xml"));
		}
		
		session = sqlSessionFactory.openSession(true);
	}
	
	protected SqlSession getSession() {
		if (session == null) {
			initSession();
		}
		
		return session;
	}
}
