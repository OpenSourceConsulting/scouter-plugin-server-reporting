package scouter.plugin.server.reporting.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public abstract class AbstractService {

	private static SqlSessionFactory sqlSessionFactory;
	
	protected SqlSession getSession() {
		if (sqlSessionFactory == null) {
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(AbstractService.class.getResourceAsStream("/mybatis-config.xml"));
		}
		
		return sqlSessionFactory.openSession(true);
	}
}
