package scouter.plugin.server.reporting.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scouter.plugin.server.reporting.service.ScouterService;
import scouter.plugin.server.reporting.vo.Alert;

public class AlertReport extends AbstractReport {

	@Override
	public void createExcel(int year, int month, int date) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month) + "." + (date < 10 ? "0" + date : date);
		
		createStyles(workbook);

		ScouterService service = new ScouterService();

		int rowIdx = 0;
		int colIdx = 0;
		
		List<Alert> alertList = service.getAlertList(year, month, date);
		Map<Integer, Integer> widthMap = new HashMap<Integer, Integer>();

		sheet = workbook.createSheet(day);
		
		row = sheet.createRow(rowIdx++);
		cell = row.createCell(colIdx++);
		cell.setCellValue("OBJECT_NAME");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(0, 5000);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("DATE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(1, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("TIME");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(2, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("LEVEL");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(3, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("TITLE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(4, 5000);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("MESSAGE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(5, 5000);
		
		if (alertList != null && !alertList.isEmpty()) {
    		for (Alert alert : alertList) {
    			colIdx = 0;
    			row = sheet.createRow(rowIdx++);
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getObject_name());
				cell.setCellStyle(styles.get("text"));
				
				if (widthMap.get(0) < alert.getObject_name().length() * 195) {
					widthMap.put(0, alert.getObject_name().length() * 195);
				}
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getDay().replaceAll("-", "."));
				cell.setCellStyle(styles.get("date"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getTime());
				cell.setCellStyle(styles.get("date"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getLevel());
				cell.setCellStyle(styles.get("text"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getTitle());
				cell.setCellStyle(styles.get("text"));
				
				if (widthMap.get(4) < alert.getTitle().length() * 250) {
					widthMap.put(4, alert.getTitle().length() * 250);
				}
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getMessage());
				cell.setCellStyle(styles.get("l_text"));
				
				if (widthMap.get(5) < alert.getMessage().length() * 190) {
					widthMap.put(5, alert.getMessage().length() * 190);
				}
    		}
    		
    		sheet.setAutoFilter(new CellRangeAddress(0, alertList.size(), 0, 5));
		}
		
		for (Integer idx : widthMap.keySet()) {
			int length = widthMap.get(idx);
			
			sheet.setColumnWidth(idx, length);
		}
		
		String dir = conf.getValue("ext_plugin_reporting_output_dir", DEFAULT_DIR);
		
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		
		dir = dir + year + File.separator;
		dir = dir + (month < 10 ? "0" + month : month) + File.separator;
		dir = dir + (date < 10 ? "0" + date : date) + File.separator;
		
		File file = new File(dir + "alert_" + day + ".xlsx");
		file.getParentFile().mkdirs();
		
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

	@Override
	public void createExcel(int year, int month) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month);
		
		createStyles(workbook);

		ScouterService service = new ScouterService();

		int rowIdx = 0;
		int colIdx = 0;
		
		List<Alert> alertList = service.getAlertList(year, month);
		Map<Integer, Integer> widthMap = new HashMap<Integer, Integer>();
		
		sheet = workbook.createSheet(day);
		
		row = sheet.createRow(rowIdx++);
		cell = row.createCell(colIdx++);
		cell.setCellValue("OBJECT_NAME");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(0, 5000);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("DATE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(1, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("TIME");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(2, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("LEVEL");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(3, 2500);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("TITLE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(4, 5000);
		
		cell = row.createCell(colIdx++);
		cell.setCellValue("MESSAGE");
		cell.setCellStyle(styles.get("header"));
		widthMap.put(5, 5000);
		
		if (alertList != null && !alertList.isEmpty()) {
    		for (Alert alert : alertList) {
    			colIdx = 0;
    			row = sheet.createRow(rowIdx++);
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getObject_name());
				cell.setCellStyle(styles.get("text"));
				
				if (widthMap.get(0) < alert.getObject_name().length() * 195) {
					widthMap.put(0, alert.getObject_name().length() * 195);
				}
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getDay().replaceAll("-", "."));
				cell.setCellStyle(styles.get("date"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getTime());
				cell.setCellStyle(styles.get("date"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getLevel());
				cell.setCellStyle(styles.get("text"));
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getTitle());
				cell.setCellStyle(styles.get("text"));
				
				if (widthMap.get(4) < alert.getTitle().length() * 250) {
					widthMap.put(4, alert.getTitle().length() * 250);
				}
    			
				cell = row.createCell(colIdx++);
				cell.setCellValue(alert.getMessage());
				cell.setCellStyle(styles.get("l_text"));
				
				if (widthMap.get(5) < alert.getMessage().length() * 190) {
					widthMap.put(5, alert.getMessage().length() * 190);
				}
    		}
    		
    		sheet.setAutoFilter(new CellRangeAddress(0, alertList.size(), 0, 5));
		}
		
		for (Integer idx : widthMap.keySet()) {
			int length = widthMap.get(idx);
			
			sheet.setColumnWidth(idx, length);
		}
		
		String dir = conf.getValue("ext_plugin_reporting_output_dir", DEFAULT_DIR);
		
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		
		dir = dir + year + File.separator;
		dir = dir + (month < 10 ? "0" + month : month) + File.separator;
		
		File file = new File(dir + "alert_" + day + ".xlsx");
		file.getParentFile().mkdirs();

		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

}
