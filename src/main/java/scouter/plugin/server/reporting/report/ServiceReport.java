package scouter.plugin.server.reporting.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;

import scouter.plugin.server.reporting.service.ScouterService;
import scouter.plugin.server.reporting.vo.Service;
import scouter.util.Hexa32;

public class ServiceReport extends AbstractReport {

	@Override
	public void createExcel(int year, int month, int date) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(ServiceReport.class.getResourceAsStream("/excel/service_hourly_template.xlsx"));
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month) + "." + (date < 10 ? "0" + date : date);
		
		createStyles(workbook);

		int sheetCnt = 0;
		String sheetName = null;
		int rowIdx = 0;
		int colIdx = 0;
		
		ScouterService scouterService = new ScouterService();
		List<Service> serviceSummaryList = scouterService.getServiceDaySummary(year, month, date);
		
		Map<String, List<Service>> serviceMap = new LinkedHashMap<String, List<Service>>();
		
		sheet = workbook.getSheetAt(sheetCnt++);	
		rowIdx = 1;
		for (Service service : serviceSummaryList) {
			if (rowIdx <= 30) {
				serviceMap.put(service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash()), new ArrayList<Service>());
			}
			
			row = sheet.createRow(rowIdx++);
			colIdx = 0;
			
			cell = row.createCell(colIdx++);
			cell.setCellValue(service.getDay().replaceAll("-", "."));
			cell.setCellStyle(styles.get("date"));
				
			if (rowIdx == serviceSummaryList.size()) {
				sheet.addMergedRegion(new CellRangeAddress(1, serviceSummaryList.size(), 0, 0));
				RegionUtil.setBorderBottom(1, new CellRangeAddress(1, serviceSummaryList.size(), 0, 0), sheet, workbook);
			}
			
			cell = row.createCell(colIdx++);
			if (service.getApp_id() != null) {
				cell.setCellValue(service.getApp_id());
				cell.setCellStyle(styles.get("text"));
			}
			
			cell = row.createCell(colIdx++);
			cell.setCellValue(service.getService_name());
			
			if (rowIdx <= 31) {
				String name = service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash());

				XSSFHyperlink link = workbook.getCreationHelper().createHyperlink(XSSFHyperlink.LINK_DOCUMENT);
				link.setAddress("'" + name + "'!A1");
				cell.setHyperlink(link);
				cell.setCellStyle(styles.get("link"));
			} else {
				cell.setCellStyle(styles.get("text"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_avg() != null) {
				cell.setCellValue(service.getElapsed_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_max() != null) {
				cell.setCellValue(service.getElapsed_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_count_avg() != null) {
				cell.setCellValue(service.getSql_count_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_count_max() != null) {
				cell.setCellValue(service.getSql_count_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_time_avg() != null) {
				cell.setCellValue(service.getSql_time_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_time_max() != null) {
				cell.setCellValue(service.getSql_time_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getRequest_count() != null) {
				cell.setCellValue(service.getRequest_count());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getError_count() != null) {
				cell.setCellValue(service.getError_count());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_exceed_count() != null) {
				cell.setCellValue(service.getElapsed_exceed_count());
				cell.setCellStyle(styles.get("numeric"));
			}
		}
		
		List<Service> serviceList = null;
		
		for (String key : serviceMap.keySet()) {
            serviceList = scouterService.getServiceHourlyStat(year, month, date, key.split("-")[0], Hexa32.toLong32(key.split("-")[1]));
            
			String name = key;

			sheet = workbook.getSheetAt(sheetCnt);
			sheetName = sheet.getSheetName();
			workbook.setSheetName(sheetCnt++, name);
			
			// remove IP_ADDRESS_GROUP, USER_AGENT_GROUP
			row = sheet.getRow(0);
			row.removeCell(row.getCell(11));
			row.removeCell(row.getCell(12));

    		rowIdx = 1;
            for (Service service : serviceList) {
        		colIdx = 2;
    			row = sheet.getRow(rowIdx++);

    			if (rowIdx == 2) {
    				cell = row.getCell(0);
    				cell.setCellValue("[" + service.getApp_id() + "]\n" + service.getService_name());
    				cell.setCellStyle(styles.get("text"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_avg() != null) {
    				cell.setCellValue(service.getElapsed_avg());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_max() != null) {
    				cell.setCellValue(service.getElapsed_max());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_count_avg() != null) {
    				cell.setCellValue(service.getSql_count_avg());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_count_max() != null) {
    				cell.setCellValue(service.getSql_count_max());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_time_avg() != null) {
    				cell.setCellValue(service.getSql_time_avg());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_time_max() != null) {
    				cell.setCellValue(service.getSql_time_max());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getRequest_count() != null) {
    				cell.setCellValue(service.getRequest_count());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getError_count() != null) {
    				cell.setCellValue(service.getError_count());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_exceed_count() != null) {
    				cell.setCellValue(service.getElapsed_exceed_count());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			
    			cell = row.getCell(colIdx++);
    			/*
    			if (service.getIp_count() != null && service.getIp_count() != 0) {
    				cell.setCellValue(service.getIp_count());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			*/
    			row.removeCell(cell);
    			
    			cell = row.getCell(colIdx++);
    			/*
    			if (service.getUa_count() != null && service.getUa_count() != 0) {
    				cell.setCellValue(service.getUa_count());
    				cell.setCellStyle(styles.get("numeric"));
    			}
    			*/
    			row.removeCell(cell);
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
		
		File file = new File(dir + "service_" + day + ".xlsx");
		file.getParentFile().mkdirs();
		
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

	@Override
	public void createExcel(int year, int month) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(ServiceReport.class.getResourceAsStream("/excel/service_daily_template.xlsx"));
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month);
		
		createStyles(workbook);

		int sheetCnt = 0;
		String sheetName = null;
		int rowIdx = 0;
		int colIdx = 0;
		
		ScouterService scouterService = new ScouterService();
		List<Service> serviceSummaryList = scouterService.getServiceMonthSummary(year, month);
		
		Map<String, List<Service>> serviceMap = new LinkedHashMap<String, List<Service>>();
		
		Map<String, String> keyMap = new HashMap<String, String>();
		
		sheet = workbook.getSheetAt(sheetCnt++);	
		rowIdx = 1;
		for (Service service : serviceSummaryList) {
			if (rowIdx <= 30) {
				serviceMap.put(service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash()), new ArrayList<Service>());
				keyMap.put(service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash()), "[" + service.getApp_id() + "]\n" + service.getService_name());
			}
			
			row = sheet.createRow(rowIdx++);
			colIdx = 0;
			
			cell = row.createCell(colIdx++);
			cell.setCellValue(service.getDay().replaceAll("-", "."));
			cell.setCellStyle(styles.get("date"));
				
			if (rowIdx == serviceSummaryList.size()) {
				sheet.addMergedRegion(new CellRangeAddress(1, serviceSummaryList.size(), 0, 0));
				RegionUtil.setBorderBottom(1, new CellRangeAddress(1, serviceSummaryList.size(), 0, 0), sheet, workbook);
			}
			
			cell = row.createCell(colIdx++);
			if (service.getApp_id() != null) {
				cell.setCellValue(service.getApp_id());
				cell.setCellStyle(styles.get("text"));
			}
			
			cell = row.createCell(colIdx++);
			cell.setCellValue(service.getService_name());
			
			if (rowIdx <= 31) {
				String name = service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash());

				XSSFHyperlink link = workbook.getCreationHelper().createHyperlink(XSSFHyperlink.LINK_DOCUMENT);
				link.setAddress("'" + name + "'!A1");
				cell.setHyperlink(link);
				cell.setCellStyle(styles.get("link"));
			} else {
				cell.setCellStyle(styles.get("text"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_avg() != null) {
				cell.setCellValue(service.getElapsed_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_max() != null) {
				cell.setCellValue(service.getElapsed_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_count_avg() != null) {
				cell.setCellValue(service.getSql_count_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_count_max() != null) {
				cell.setCellValue(service.getSql_count_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_time_avg() != null) {
				cell.setCellValue(service.getSql_time_avg());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getSql_time_max() != null) {
				cell.setCellValue(service.getSql_time_max());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getRequest_count() != null) {
				cell.setCellValue(service.getRequest_count());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getError_count() != null) {
				cell.setCellValue(service.getError_count());
				cell.setCellStyle(styles.get("numeric"));
			}
			
			cell = row.createCell(colIdx++);
			if (service.getElapsed_exceed_count() != null) {
				cell.setCellValue(service.getElapsed_exceed_count());
				cell.setCellStyle(styles.get("numeric"));
			}
		}
		
		Map<String, List<Service>> resultMap = scouterService.getServiceDailyStat(year, month);
		
		List<Service> serviceList = null;
		
		for (String key : serviceMap.keySet()) {
			for (String idx : resultMap.keySet()) {
				serviceList = resultMap.get(idx);
				
				boolean exist = false;
				for (Service service : serviceList) {
					if (service.getService_hash() != null && key.equals(service.getApp_id() + "-" + Hexa32.toString32(service.getService_hash()))) {
						exist = true;
						serviceMap.get(key).add(service);
					}
				}
				
				if (!exist) {
					Service s = new Service();
					s.setDay(idx);
					s.setService_name(keyMap.get(key));
					serviceMap.get(key).add(s);
				}
			}
		}
		
		for (String key : serviceMap.keySet()) {
            serviceList = serviceMap.get(key);
            
			String name = key;

			sheet = workbook.getSheetAt(sheetCnt);
			sheetName = sheet.getSheetName();
			workbook.setSheetName(sheetCnt++, name);
			
			int diff = (31 - serviceList.size());
			
			// remove IP_ADDRESS_GROUP, USER_AGENT_GROUP
			row = sheet.getRow(0);
			row.removeCell(row.getCell(11));
			row.removeCell(row.getCell(12));

    		rowIdx = 1;
            for (Service service : serviceList) {
    			if (rowIdx == 3) {
    				if (diff > 0) {
    					sheet.removeMergedRegion(0);
    					sheet.addMergedRegion(new CellRangeAddress(1, serviceList.size(), 0, 0));
    					RegionUtil.setBorderBottom(1, new CellRangeAddress(1, serviceList.size(), 0, 0), sheet, workbook);
    				}
    			}
    			
        		colIdx = 1;
    			row = sheet.getRow(rowIdx++);

    			if (rowIdx == 2) {
    				cell = row.getCell(0);
    				cell.setCellValue(service.getService_name());
    				cell.setCellStyle(styles.get("text"));
    			}
    			
    			cell = row.getCell(colIdx++);
				cell.setCellValue(service.getDay().replaceAll("-", "."));
				cell.setCellStyle(styles.get("date"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_avg() != null) {
    				cell.setCellValue(service.getElapsed_avg());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_max() != null) {
    				cell.setCellValue(service.getElapsed_max());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_count_avg() != null) {
    				cell.setCellValue(service.getSql_count_avg());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_count_max() != null) {
    				cell.setCellValue(service.getSql_count_max());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_time_avg() != null) {
    				cell.setCellValue(service.getSql_time_avg());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getSql_time_max() != null) {
    				cell.setCellValue(service.getSql_time_max());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getRequest_count() != null) {
    				cell.setCellValue(service.getRequest_count());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getError_count() != null) {
    				cell.setCellValue(service.getError_count());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			if (service.getElapsed_exceed_count() != null) {
    				cell.setCellValue(service.getElapsed_exceed_count());
    			}
				cell.setCellStyle(styles.get("numeric"));
    			
    			cell = row.getCell(colIdx++);
    			/*
    			if (service.getIp_count() != null) {
    				cell.setCellValue(service.getIp_count());
    			}
				cell.setCellStyle(styles.get("numeric"));
				*/
    			row.removeCell(cell);
    			
    			cell = row.getCell(colIdx++);
    			/*
    			if (service.getUa_count() != null) {
    				cell.setCellValue(service.getUa_count());
    			}
				cell.setCellStyle(styles.get("numeric"));
				*/
    			row.removeCell(cell);
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
		
		File file = new File(dir + "service_" + day + ".xlsx");
		file.getParentFile().mkdirs();
		
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}
}
