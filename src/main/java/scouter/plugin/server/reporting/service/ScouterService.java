package scouter.plugin.server.reporting.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import scouter.plugin.server.reporting.vo.AgentInfo;
import scouter.plugin.server.reporting.vo.Alert;
import scouter.plugin.server.reporting.vo.HostAgent;
import scouter.plugin.server.reporting.vo.JavaAgent;
import scouter.plugin.server.reporting.vo.Service;

public class ScouterService extends AbstractService {

	public List<AgentInfo> getAgentInfoList() {
		return getSession().selectList("Scouter.selectAgentInfoList");
	}
	
	public AgentInfo getAgentInfo(int objHash) {
		return getSession().selectOne("Scouter.selectAgentInfo", objHash);
	}
	
	public List<HostAgent> getHostDailyStat(int year, int month, int objHash) {
		List<HostAgent> hostAgentList = new ArrayList<HostAgent>();
		
		int date = 1;
        int maxDay = 0;  
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String m, d = null;
        
        if (month < 10) {
        	m = "0" + month;
        } else {
        	m = month + "";
        }

		Map<String, Object> param = null;
        for (int i = 1; i <= maxDay; i++) {
			param = new HashMap<String, Object>();
			param.put("object_hash", objHash);
			param.put("year", Integer.toString(year));
			param.put("month", m);

	        if (i < 10) {
	        	d = "0" + i;
	        } else {
	        	d = i + "";
	        }
	        
			param.put("date", d);
			
			hostAgentList.add((HostAgent) getSession().selectOne("Scouter.selectHostDailyStat", param));
		}
        
        return hostAgentList;
	}

	public List<JavaAgent> getJavaDailyStat(int year, int month, int objHash) {
		List<JavaAgent> javaAgentList = new ArrayList<JavaAgent>();
		
		int date = 1;
        int maxDay = 0;  
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String m, d = null;
        
        if (month < 10) {
        	m = "0" + month;
        } else {
        	m = month + "";
        }

		Map<String, Object> param = null;
        for (int i = 1; i <= maxDay; i++) {
			param = new HashMap<String, Object>();
			param.put("object_hash", objHash);
			param.put("year", Integer.toString(year));
			param.put("month", m);

	        if (i < 10) {
	        	d = "0" + i;
	        } else {
	        	d = i + "";
	        }
	        
			param.put("date", d);
			
			javaAgentList.add((JavaAgent) getSession().selectOne("Scouter.selectJavaDailyStat", param));
		}
        
        return javaAgentList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, List<Service>> getServiceDailyStat(int year, int month) {
		Map<String, List<Service>> serviceMap = new LinkedHashMap<String, List<Service>>();
		
		int date = 1;
        int maxDay = 0;  
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String m, d = null;
        
        if (month < 10) {
        	m = "0" + month;
        } else {
        	m = month + "";
        }

		Map<String, Object> param = null;
        for (int i = 1; i <= maxDay; i++) {
			param = new HashMap<String, Object>();
			param.put("year", Integer.toString(year));
			param.put("month", m);

	        if (i < 10) {
	        	d = "0" + i;
	        } else {
	        	d = i + "";
	        }
	        
			param.put("date", d);
			
			serviceMap.put(Integer.toString(year) + "." + m + "." + d, (List) getSession().selectList("Scouter.selectServiceDailyStat", param));
		}
        
        return serviceMap;
	}

	public List<Service> getServiceMonthSummary(int year, int month) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }
        
        return getSession().selectList("Scouter.selectServiceMonthSummary", param);
	}

	public List<Service> getServiceDaySummary(int year, int month, int date) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }

        if (date < 10) {
    		param.put("date", "0" + date);
        } else {
    		param.put("date", date + "");
        }
        
        return getSession().selectList("Scouter.selectServiceDaySummary", param);
	}
	
	public List<HostAgent> getHostHourlyStat(int year, int month, int date, int objHash) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("object_hash", objHash);
		param.put("year", Integer.toString(year));
		param.put("month", month);
		param.put("date", date < 10 ? "0" + date : date);
		
		return getSession().selectList("Scouter.selectHostHourlyStat", param);
	}
	
	public List<JavaAgent> getJavaHourlyStat(int year, int month, int date, int objHash) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("object_hash", objHash);
		param.put("year", Integer.toString(year));
		param.put("month", month);
		param.put("date", date < 10 ? "0" + date : date);
		
		return getSession().selectList("Scouter.selectJavaHourlyStat", param);
	}

	public List<Service> getServiceHourlyStat(int year, int month, int date, String appId, long hash) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }

        if (date < 10) {
    		param.put("date", "0" + date);
        } else {
    		param.put("date", date + "");
        }
        
        param.put("app_id", appId);
        param.put("service_hash", hash);
        
        return getSession().selectList("Scouter.selectServiceHourlyStat", param);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Alert> getAlertList(int year, int month) {
		List<Alert> alertList = new ArrayList<Alert>();
		
		int date = 1;
        int maxDay = 0;  
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String m, d = null;
        
        if (month < 10) {
        	m = "0" + month;
        } else {
        	m = month + "";
        }

		Map<String, Object> param = null;
        for (int i = 1; i <= maxDay; i++) {
			param = new HashMap<String, Object>();
			param.put("year", Integer.toString(year));
			param.put("month", m);

	        if (i < 10) {
	        	d = "0" + i;
	        } else {
	        	d = i + "";
	        }
	        
			param.put("date", d);
			
			alertList.addAll((List) getSession().selectList("Scouter.selectAlert", param));
		}
        
        return alertList;
	}

	public List<Alert> getAlertList(int year, int month, int date) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }

        if (date < 10) {
    		param.put("date", "0" + date);
        } else {
    		param.put("date", date + "");
        }
        
        return getSession().selectList("Scouter.selectAlert", param);
	}

	public List<Service> getApplicationOperationStat(int year, int month) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }
        
        return getSession().selectList("Scouter.selectApplicationOperationStat", param);
	}

	public Service getApplicationOperationStatPrev(int year, int month, String appId, Integer hash) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }
        
        param.put("app_id", appId);
        param.put("service_hash", hash);
        
        return getSession().selectOne("Scouter.selectApplicationOperationStatPrev", param);
	}

	public List<Service> getWorstApplications(int year, int month) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("year", Integer.toString(year));
		
        if (month < 10) {
    		param.put("month", "0" + month);
        } else {
    		param.put("month", month + "");
        }
        
        return getSession().selectList("Scouter.selectWorstApplications", param);
	}
	
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.getTime());
        
        calendar.set(2016, 1, 1);
        
        System.out.println(calendar.getTime());
        
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        System.out.println(maxDay);
	}
}
