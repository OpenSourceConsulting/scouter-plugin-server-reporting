package scouter.plugin.server.reporting.report;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import scouter.server.Configure;

public abstract class AbstractReport {

	public static final String DEFAULT_DIR = System.getProperty("user.home") + File.separator + "scouter";
    public static Configure conf = Configure.getInstance();
	protected Map<String, CellStyle> styles;

	public abstract void createExcel(int year, int month, int date) throws Exception;
	public abstract void createExcel(int year, int month) throws Exception;
	
	protected void createStyles(Workbook wb) {
		styles = new HashMap<String, CellStyle>();

        CellStyle style;
        Font font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        //font.setBold(true);
        //font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        styles.put("header", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        style.setFont(font);
		CreationHelper createHelper = wb.getCreationHelper();
		style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy.mm.dd"));
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        styles.put("date", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setWrapText(true);
        styles.put("text", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        styles.put("l_text", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        styles.put("link", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
		DataFormat dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0"));
        styles.put("numeric", style);
        
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
		dataFormat = wb.createDataFormat();
		style.setDataFormat(dataFormat.getFormat("#,###,##0.0#"));
        styles.put("numeric2", style);
    }
}
