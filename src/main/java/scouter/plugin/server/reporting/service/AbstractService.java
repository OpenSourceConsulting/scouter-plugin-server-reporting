package scouter.plugin.server.reporting.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scouter.plugin.server.reporting.ReportingPlugin;

public abstract class AbstractService {

	private static SqlSessionFactory sqlSessionFactory;
	
	protected synchronized SqlSession getSession() {
		if (sqlSessionFactory == null) {
			sqlSessionFactory = ReportingPlugin.getSqlSessionFactory();
		}
		
		return sqlSessionFactory.openSession(true);
	}
}
