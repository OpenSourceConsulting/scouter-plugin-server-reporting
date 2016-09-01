package scouter.plugin.server.reporting.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scouter.plugin.server.reporting.service.ScouterService;
import scouter.plugin.server.reporting.vo.Service;
import scouter.server.Configure;

public class OperationReport {
	
	private static final String DEFAULT_DIR = System.getProperty("user.home") + File.separator + "scouter";
	private static Configure conf = Configure.getInstance();
	private Map<String, CellStyle> styles;

	public void createExcel(int year, int month) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		String day = year + "." + (month < 10 ? "0" + month : month);
		
		styles = createStyles(workbook);
		
		String applicationName = conf.getValue("ext_plugin_reporting_application_name");
        
        String text = null;
        
        if (applicationName == null) {
        	text = "Application 운영현황 " + year + "." + (month < 10 ? "0" + month : month) + "월";
    		sheet = workbook.createSheet(year + "." + (month < 10 ? "0" + month : month));
        } else {
        	text = "Application 운영현황(" + applicationName + ") " + year + "." + (month < 10 ? "0" + month : month) + "월";
    		sheet = workbook.createSheet(applicationName + "_" + year + "." + (month < 10 ? "0" + month : month));
        }
        
		sheet.setColumnWidth(0, 4800);
		sheet.setColumnWidth(1, 12100);
		sheet.setColumnWidth(2, 3720);
		sheet.setColumnWidth(3, 3720);
		sheet.setColumnWidth(4, 3720);
		sheet.setColumnWidth(5, 3720);

        //turn off gridlines
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);

        //the following three statements are required only for HSSF
        sheet.setAutobreaks(true);
        printSetup.setFitHeight((short)1);
        printSetup.setFitWidth((short)1);

        row = sheet.createRow(0);
        row.setHeightInPoints(80);
        cell = row.createCell(0);
        
        //cell.setCellValue(text);
        cell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$F$1"));
        
		RichTextString richString = new XSSFRichTextString(text);
		Font fontBig = workbook.createFont();
		fontBig.setFontHeightInPoints((short)36);
		fontBig.setColor(IndexedColors.DARK_BLUE.getIndex());
		
		Font fontSmall = workbook.createFont();
		fontSmall.setFontHeightInPoints((short)14);
		fontSmall.setColor(IndexedColors.DARK_BLUE.getIndex());
		
		int idx = text.lastIndexOf(Integer.toString(year));
        
		richString.applyFont(0, idx, fontBig);
		richString.applyFont(idx, text.length(), fontSmall);
		cell.setCellValue(richString);
		
		row = sheet.createRow(1);
		cell = row.createCell(0);
		
		text = "[호출 빈도 Top 10 서비스]";
		richString = new XSSFRichTextString(text);
		
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short)14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);

		richString.applyFont(0, text.length(), font);
		cell.setCellValue(richString);
        cell.setCellStyle(styles.get("text_left"));

		cell = row.createCell(5);

		text = "(단위 : 건, ms)";
		richString = new XSSFRichTextString(text);
		
		font = workbook.createFont();
		font.setFontHeightInPoints((short)14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);

		richString.applyFont(0, text.length(), font);
		cell.setCellValue(richString);
        cell.setCellStyle(styles.get("text_right"));
        
        row = sheet.createRow(2);
        cell = row.createCell(0);
        cell.setCellValue("Application ID");
        cell.setCellStyle(styles.get("table_header_TL"));
        
        cell = row.createCell(1);
        cell.setCellValue("서비스 URL");
        cell.setCellStyle(styles.get("table_header_T"));
        
        cell = row.createCell(2);
        cell.setCellValue("전 월");
        cell.setCellStyle(styles.get("table_header_T"));

        cell = row.createCell(3);
        cell.setCellStyle(styles.get("table_header_T"));
        
        cell = row.createCell(4);
        cell.setCellValue("당 월");
        cell.setCellStyle(styles.get("table_header_T"));

        cell = row.createCell(5);
        cell.setCellStyle(styles.get("table_header_TR"));
        
        row = sheet.createRow(3);
        cell = row.createCell(0);
        cell.setCellStyle(styles.get("table_header_L"));
        
        cell = row.createCell(1);
        cell.setCellStyle(styles.get("table_header"));
        
        cell = row.createCell(2);
        cell.setCellValue("요청건수");
        cell.setCellStyle(styles.get("table_header"));
        
        cell = row.createCell(3);
        cell.setCellValue("평균응답시간");
        cell.setCellStyle(styles.get("table_header"));
        
        cell = row.createCell(4);
        cell.setCellValue("요청건수");
        cell.setCellStyle(styles.get("table_header"));
        
        cell = row.createCell(5);
        cell.setCellValue("평균응답시간");
        cell.setCellStyle(styles.get("table_header_R"));
        
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$3:$A$4"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$B$3:$B$4"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$3:$D$3"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$E$3:$F$3"));
        
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1, 0, 0, 0);
		cal.add(Calendar.MONTH, -1);

		ScouterService scouterService = new ScouterService();
        List<Service> serviceList = scouterService.getApplicationOperationStat(year, month);
		Service service = null;
		Service prevService = null;
        for (int i = 0; i < 10; i++) {
        	row = sheet.createRow(4 + i);
        	
        	if (serviceList.size() > i) {
        		service = serviceList.get(i);
        		prevService = scouterService.getApplicationOperationStatPrev(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, service.getApp_id(), service.getService_hash());
        		
        		if (prevService == null) {
        			prevService = new Service();
        		}
        	} else {
        		service = new Service();
        		prevService = new Service();
        	}
        	
        	for (int j = 0; j < 6; j++) {
        		cell = row.createCell(j);
        		
        		if (i < 9) {
        			if (j == 0) {
        				cell.setCellValue(service.getApp_id());
        				cell.setCellStyle(styles.get("table_body_L"));
        			} else if (j == 1) {
        				cell.setCellValue(service.getService_name());
        				cell.setCellStyle(styles.get("table_body"));
    				} else if (j == 2) {
    					if (prevService.getRequest_count() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(prevService.getRequest_count());
    					}
        				cell.setCellStyle(styles.get("table_body_right"));
    				} else if (j == 3) {
    					if (prevService.getElapsed_avg() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(prevService.getElapsed_avg());
    					}
        				cell.setCellStyle(styles.get("table_body_right"));
    				} else if (j == 4) {
    					if (service.getRequest_count() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(service.getRequest_count());
    					}
        				cell.setCellStyle(styles.get("table_body_right"));
    				} else if (j == 5) {
    					if (service.getElapsed_avg() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(service.getElapsed_avg());
    					}
        				cell.setCellStyle(styles.get("table_body_R"));
        			}
        		} else {
        			if (j == 0) {
        				cell.setCellValue(service.getApp_id());
        				cell.setCellStyle(styles.get("table_body_LB"));
        			} else if (j == 1) {
        				cell.setCellValue(service.getService_name());
        				cell.setCellStyle(styles.get("table_body_B"));
    				} else if (j == 2) {
    					if (prevService.getRequest_count() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(prevService.getRequest_count());
    					}
        				cell.setCellStyle(styles.get("table_body_B_right"));
    				} else if (j == 3) {
    					if (prevService.getElapsed_avg() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(prevService.getElapsed_avg());
    					}
        				cell.setCellStyle(styles.get("table_body_B_right"));
    				} else if (j == 4) {
    					if (service.getRequest_count() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(service.getRequest_count());
    					}
        				cell.setCellStyle(styles.get("table_body_B_right"));
    				} else if (j == 5) {
    					if (service.getElapsed_avg() == null) {
    						cell.setCellValue("");
    					} else {
    						cell.setCellValue(service.getElapsed_avg());
    					}
        				cell.setCellStyle(styles.get("table_body_RB"));
        			}
        		}
        	}
        }
        
        row = sheet.createRow(15);
        row.setHeightInPoints(40);
        
        for (int i = 0; i < 6; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(styles.get("worst"));
        }
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$16:$F$16"));
        
        cell = row.getCell(0);

        text = "Worst 프로그램 산출기준 : 에러건수가 요청건수의 30% 이상이거나, 평균 응답시간이 10초 이상인 경우";
        cell.setCellStyle(styles.get("worst"));
        
		richString = new XSSFRichTextString(text);
		fontBig = workbook.createFont();
		fontBig.setBoldweight(Font.BOLDWEIGHT_BOLD);
		fontBig.setFontHeightInPoints((short)14);
		
		fontSmall = workbook.createFont();
		fontSmall.setFontHeightInPoints((short)11);
		
		richString.applyFont(0, 17, fontBig);
		richString.applyFont(17, text.length(), fontSmall);
		cell.setCellValue(richString);
		
		row = sheet.createRow(17);
		cell = row.createCell(0);
		
		text = "[당월 응답속도 Worst 서비스]";
		richString = new XSSFRichTextString(text);
		
		font = workbook.createFont();
		font.setFontHeightInPoints((short)14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);

		richString.applyFont(0, text.length(), font);
		cell.setCellValue(richString);
        cell.setCellStyle(styles.get("text_left"));

		cell = row.createCell(5);

		text = "(단위 : 건, ms)";
		richString = new XSSFRichTextString(text);
		
		font = workbook.createFont();
		font.setFontHeightInPoints((short)14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);

		richString.applyFont(0, text.length(), font);
		cell.setCellValue(richString);
        cell.setCellStyle(styles.get("text_right"));
		
        row = sheet.createRow(18);
        cell = row.createCell(0);
        cell.setCellValue("Application ID");
        cell.setCellStyle(styles.get("table_header_TL"));
        
        cell = row.createCell(1);
        cell.setCellValue("서비스 URL");
        cell.setCellStyle(styles.get("table_header_T"));
        
        cell = row.createCell(2);
        cell.setCellValue("요청건수");
        cell.setCellStyle(styles.get("table_header_T"));

        cell = row.createCell(3);
        cell.setCellValue("에러건수");
        cell.setCellStyle(styles.get("table_header_T"));
        
        cell = row.createCell(4);
        cell.setCellValue("평균응답시간");
        cell.setCellStyle(styles.get("table_header_T"));

        cell = row.createCell(5);
        cell.setCellValue("최대응답시간");
        cell.setCellStyle(styles.get("table_header_TR"));
        
        serviceList = scouterService.getWorstApplications(year, month);
        
        if (serviceList == null || serviceList.size() == 0) {
        	row = sheet.createRow(19);
        	
        	for (int j = 0; j < 6; j++) {
        		cell = row.createCell(j);
        		
    			if (j == 0) {
    				cell.setCellValue("No Data");
    				cell.setCellStyle(styles.get("table_body_LB"));
    			} else if (j == 5) {
    				cell.setCellStyle(styles.get("table_body_RB"));
    			} else {
    				cell.setCellStyle(styles.get("table_body_B"));
    			}
        	}

            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$20:$F$20"));
        } else {
        	for (int i = 0; i < serviceList.size(); i++) {
            	row = sheet.createRow(19 + i);

        		service = serviceList.get(i);
            	for (int j = 0; j < 6; j++) {
            		cell = row.createCell(j);
            		
            		if (i < (serviceList.size() - 1)) {
            			if (j == 0) {
            				cell.setCellValue(service.getApp_id());
            				cell.setCellStyle(styles.get("table_body_L"));
            			} else if (j == 1) {
            				cell.setCellValue(service.getService_name());
            				cell.setCellStyle(styles.get("table_body"));
        				} else if (j == 2) {
        					if (service.getRequest_count() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getRequest_count());
        					}
            				cell.setCellStyle(styles.get("table_body_right"));
        				} else if (j == 3) {
        					if (service.getError_count() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getError_count());
        					}
            				cell.setCellStyle(styles.get("table_body_right"));
        				} else if (j == 4) {
        					if (service.getElapsed_avg() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getElapsed_avg());
        					}
            				cell.setCellStyle(styles.get("table_body_right"));
        				} else if (j == 5) {
        					if (service.getElapsed_max() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getElapsed_max());
        					}
            				cell.setCellStyle(styles.get("table_body_R"));
            			}
            		} else {
            			if (j == 0) {
            				cell.setCellValue(service.getApp_id());
            				cell.setCellStyle(styles.get("table_body_LB"));
            			} else if (j == 1) {
            				cell.setCellValue(service.getService_name());
            				cell.setCellStyle(styles.get("table_body_B"));
        				} else if (j == 2) {
        					if (service.getRequest_count() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getRequest_count());
        					}
            				cell.setCellStyle(styles.get("table_body_B_right"));
        				} else if (j == 3) {
        					if (service.getError_count() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getError_count());
        					}
            				cell.setCellStyle(styles.get("table_body_B_right"));
        				} else if (j == 4) {
        					if (service.getElapsed_avg() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getElapsed_avg());
        					}
            				cell.setCellStyle(styles.get("table_body_B_right"));
        				} else if (j == 5) {
        					if (service.getElapsed_max() == null) {
        						cell.setCellValue("");
        					} else {
        						cell.setCellValue(service.getElapsed_max());
        					}
            				cell.setCellStyle(styles.get("table_body_RB"));
            			}
            		}
            	}
        	}
        }
		
		String dir = conf.getValue("ext_plugin_reporting_output_dir", DEFAULT_DIR);
		
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		
		dir = dir + year + File.separator;
		dir = dir + (month < 10 ? "0" + month : month) + File.separator;
		
		File file = new File(dir + "operation_" + day + ".xlsx");
		file.getParentFile().mkdirs();

		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

    /**
     * cell styles used for formatting calendar sheets
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

        short borderColor = IndexedColors.BLACK.getIndex();
        
        CellStyle style;
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 41);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(borderColor);
        styles.put("worst", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styles.put("text_left", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styles.put("text_right", style);

        Font textFont = wb.createFont();
        textFont.setFontHeightInPoints((short)12);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header_TL", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header_TR", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header_T", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header_L", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor((short) 44);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(textFont);
        styles.put("table_header_R", style);

        style = wb.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("table_body", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
		DataFormat dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0"));
        styles.put("table_body_right", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("table_body_L", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0"));
        styles.put("table_body_R", style);

        style = wb.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(borderColor);
        styles.put("table_body_B", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(borderColor);
        dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0"));
        styles.put("table_body_B_right", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(borderColor);
        styles.put("table_body_LB", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setBorderBottom(CellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(borderColor);
        dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0"));
        styles.put("table_body_RB", style);

        return styles;
    }
    
    public static void main(String[] args) throws Exception {
    	new OperationReport().createExcel(2016, 8);
	}
}