package scouter.plugin.server.reporting.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;

import scouter.plugin.server.reporting.service.ScouterService;
import scouter.plugin.server.reporting.vo.AgentInfo;
import scouter.plugin.server.reporting.vo.JavaAgent;

public class JavaReport extends AbstractReport {

	@Override
	public void createExcel(int year, int month, int date) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(JavaReport.class.getResourceAsStream("/excel/java_hourly_template.xlsx"));
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month) + "." + (date < 10 ? "0" + date : date);
		
		createStyles(workbook);

		ScouterService service = new ScouterService();
		List<AgentInfo> agentInfoList = service.getAgentInfoList();

		AgentInfo agentInfo = null;

		int sheetCnt = 0;
		String sheetName = null;
		int rowIdx = 0;
		int colIdx = 0;
		List<JavaAgent> agentList = null;
		for (int i = 0; i < agentInfoList.size(); i++) {
			agentInfo = agentInfoList.get(i);

        	if (agentInfo.getObject_family().equals("javaee") || agentInfo.getObject_family().equals("java")) {
        		agentList = service.getJavaHourlyStat(year, month, date, agentInfo.getObject_hash());
        		
        		if (agentList != null && !agentList.isEmpty()) {
        			String name = agentList.get(0).getObject_name();
        			
        			if (name.indexOf("/") > -1) {
        				name = name.substring(name.lastIndexOf("/") + 1);
        			}
        			
	    			sheet = workbook.getSheetAt(sheetCnt);
	    			sheetName = sheet.getSheetName();
        			workbook.setSheetName(sheetCnt++, name);
	    			
	    			rowIdx = 1;
	        		for (JavaAgent agent : agentList) {
	        			colIdx = 3;
	        			row = sheet.getRow(rowIdx++);
	        			
	        			if (rowIdx == 2) {
	        				cell = row.getCell(0);
	        				cell.setCellValue(agentList.get(0).getObject_name());
	        				cell.setCellStyle(styles.get("text"));
	        				
	        				cell = row.getCell(1);
	        				cell.setCellValue(year + "." + (month < 10 ? "0" + month : month) + "." + (date < 10 ? "0" + date : date));
	        				cell.setCellStyle(styles.get("date"));
	        			}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getActive_service_avg() != null) {
	        				cell.setCellValue(agent.getActive_service_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getActive_service_max() != null) {
	        				cell.setCellValue(agent.getActive_service_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_total() != null) {
	        				cell.setCellValue(agent.getHeap_total());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_used_avg() != null) {
	        				cell.setCellValue(agent.getHeap_used_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_used_max() != null) {
	        				cell.setCellValue(agent.getHeap_used_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getRecent_user_avg() != null) {
	        				cell.setCellValue(agent.getRecent_user_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getRecent_user_max() != null) {
	        				cell.setCellValue(agent.getRecent_user_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getService_count_avg() != null) {
	        				cell.setCellValue(agent.getService_count_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getService_count_max() != null) {
	        				cell.setCellValue(agent.getService_count_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getApi_tps_avg() != null) {
	        				cell.setCellValue(agent.getApi_tps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getApi_tps_max() != null) {
	        				cell.setCellValue(agent.getApi_tps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getSql_tps_avg() != null) {
	        				cell.setCellValue(agent.getSql_tps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getSql_tps_max() != null) {
	        				cell.setCellValue(agent.getSql_tps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getTps_avg() != null) {
	        				cell.setCellValue(agent.getTps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getTps_max() != null) {
	        				cell.setCellValue(agent.getTps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        		}

	        		XSSFDrawing drawing = sheet.getDrawingPatriarch();
	        		List<XSSFChart> chartList = drawing.getCharts();
	        		for (XSSFChart chart : chartList) {
	        			CTLineChart[] lineChartList = chart.getCTChart().getPlotArea().getLineChartArray();
	        			for (CTLineChart c : lineChartList) {
	        				CTLineSer[] seriesList = c.getSerArray();
	        				for (CTLineSer ser : seriesList) {
	        					String ref = ser.getTx().getStrRef().getF();
	        					ser.getTx().getStrRef().setF(ref.replaceAll("'" + sheetName + "'", "'" + name + "'"));
	        					ref = ser.getCat().getStrRef().getF();
	        					ser.getCat().getStrRef().setF(ref.replaceAll("'" + sheetName + "'", "'" + name + "'"));
	        					ref = ser.getVal().getNumRef().getF();
	        					ser.getVal().getNumRef().setF(ref.replaceAll("'" + sheetName + "'", "'" + name + "'"));
	        				}
	        			}
	        		}
        		}
            }
		}
		
		int totalCnt = workbook.getNumberOfSheets();
		for (int i = sheetCnt; i < totalCnt; i++) {
			workbook.removeSheetAt(sheetCnt);
		}
		
		String dir = conf.getValue("ext_plugin_reporting_output_dir", DEFAULT_DIR);
		
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		
		dir = dir + year + File.separator;
		dir = dir + (month < 10 ? "0" + month : month) + File.separator;
		dir = dir + (date < 10 ? "0" + date : date) + File.separator;
		
		File file = new File(dir + "java_" + day + ".xlsx");
		file.getParentFile().mkdirs();
		
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

	@Override
	public void createExcel(int year, int month) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(JavaReport.class.getResourceAsStream("/excel/java_daily_template.xlsx"));
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month);
		
		createStyles(workbook);

		ScouterService service = new ScouterService();
		List<AgentInfo> agentInfoList = service.getAgentInfoList();

		AgentInfo agentInfo = null;

		int sheetCnt = 0;
		String sheetName = null;
		int rowIdx = 0;
		int colIdx = 0;
		List<JavaAgent> agentList = null;
		for (int i = 0; i < agentInfoList.size(); i++) {
			agentInfo = agentInfoList.get(i);

        	if (agentInfo.getObject_family().equals("javaee") || agentInfo.getObject_family().equals("java")) {
        		agentList = service.getJavaDailyStat(year, month, agentInfo.getObject_hash());
        		
        		if (agentList != null && !agentList.isEmpty()) {
        			String name = agentList.get(0).getObject_name();
        			
        			if (name.indexOf("/") > -1) {
        				name = name.substring(name.lastIndexOf("/") + 1);
        			}
        			
	    			sheet = workbook.getSheetAt(sheetCnt);
	    			sheetName = sheet.getSheetName();
        			workbook.setSheetName(sheetCnt++, name);
        			
    				int diff = (31 - agentList.size());
	    			
	    			rowIdx = 1;
	        		for (JavaAgent agent : agentList) {	
	        			if (rowIdx == 3) {
	        				if (diff > 0) {
	        					sheet.removeMergedRegion(0);
	        					sheet.addMergedRegion(new CellRangeAddress(1, agentList.size(), 0, 0));
	        					RegionUtil.setBorderBottom(1, new CellRangeAddress(1, agentList.size(), 0, 0), sheet, workbook);
	        				}
	        			}
	        			
	        			colIdx = 1;
	        			row = sheet.getRow(rowIdx++);
	        			
	        			if (rowIdx == 2) {
	        				cell = row.getCell(0);
	        				cell.setCellValue(agentList.get(0).getObject_name());
	        				cell.setCellStyle(styles.get("text"));
	        			}
        				
        				cell = row.getCell(colIdx++);
        				cell.setCellValue(agent.getDay().replaceAll("-", "."));
        				cell.setCellStyle(styles.get("date"));
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getActive_service_avg() != null) {
	        				cell.setCellValue(agent.getActive_service_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getActive_service_max() != null) {
	        				cell.setCellValue(agent.getActive_service_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_total() != null) {
	        				cell.setCellValue(agent.getHeap_total());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_used_avg() != null) {
	        				cell.setCellValue(agent.getHeap_used_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getHeap_used_max() != null) {
	        				cell.setCellValue(agent.getHeap_used_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getRecent_user_avg() != null) {
	        				cell.setCellValue(agent.getRecent_user_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getRecent_user_max() != null) {
	        				cell.setCellValue(agent.getRecent_user_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getService_count_avg() != null) {
	        				cell.setCellValue(agent.getService_count_avg());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getService_count_max() != null) {
	        				cell.setCellValue(agent.getService_count_max());
	        				cell.setCellStyle(styles.get("numeric"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getApi_tps_avg() != null) {
	        				cell.setCellValue(agent.getApi_tps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getApi_tps_max() != null) {
	        				cell.setCellValue(agent.getApi_tps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getSql_tps_avg() != null) {
	        				cell.setCellValue(agent.getSql_tps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getSql_tps_max() != null) {
	        				cell.setCellValue(agent.getSql_tps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getTps_avg() != null) {
	        				cell.setCellValue(agent.getTps_avg());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        			
        				cell = row.getCell(colIdx++);
        				if (agent.getTps_max() != null) {
	        				cell.setCellValue(agent.getTps_max());
	        				cell.setCellStyle(styles.get("numeric2"));
        				}
	        		}
        			
        			if (diff > 0) {
        				for (int r = 0; r < diff; r++) {
        					row = sheet.getRow(rowIdx++);
        					sheet.removeRow(row);
        				}
        			}

	        		XSSFDrawing drawing = sheet.getDrawingPatriarch();
	        		List<XSSFChart> chartList = drawing.getCharts();
	        		for (XSSFChart chart : chartList) {
	        			CTLineChart[] lineChartList = chart.getCTChart().getPlotArea().getLineChartArray();
	        			for (CTLineChart c : lineChartList) {
	        				CTLineSer[] seriesList = c.getSerArray();
	        				for (CTLineSer ser : seriesList) {
	        					String ref = ser.getTx().getStrRef().getF().replaceAll("'" + sheetName + "'", "'" + name + "'");
	        					
	        					if (diff > 0) {
	        						ref = ref.replaceAll("32", Integer.toString(32 - diff));
	        					}
	        					ser.getTx().getStrRef().setF(ref);
	        					
	        					ref = ser.getCat().getNumRef().getF().replaceAll("'" + sheetName + "'", "'" + name + "'");
	        					
	        					if (diff > 0) {
	        						ref = ref.replaceAll("32", Integer.toString(32 - diff));
	        					}
	        					ser.getCat().getNumRef().setF(ref);
	        					
	        					
	        					ref = ser.getVal().getNumRef().getF().replaceAll("'" + sheetName + "'", "'" + name + "'");
	        					
	        					if (diff > 0) {
	        						ref = ref.replaceAll("32", Integer.toString(32 - diff));
	        					}
	        					ser.getVal().getNumRef().setF(ref);
	        				}
	        			}
	        		}
        		}
            }
		}
		
		int totalCnt = workbook.getNumberOfSheets();
		for (int i = sheetCnt; i < totalCnt; i++) {
			workbook.removeSheetAt(sheetCnt);
		}
		
		String dir = conf.getValue("ext_plugin_reporting_output_dir", DEFAULT_DIR);
		
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		
		dir = dir + year + File.separator;
		dir = dir + (month < 10 ? "0" + month : month) + File.separator;
		
		File file = new File(dir + "java_" + day + ".xlsx");
		file.getParentFile().mkdirs();
		
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}
}
