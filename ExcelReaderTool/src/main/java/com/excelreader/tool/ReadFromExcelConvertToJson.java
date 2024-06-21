package com.excelreader.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class ReadFromExcelConvertToJson {

	private static final String SAMPLE_XLSX_FILE_PATH = "C:\\Users\\ss186034\\OneDrive - NCR ATLEOS\\Documents\\Tickets\\partnerUsersQ1.xlsx";

    public static void main(String[] args) {
        Object json = processSmallFile(SAMPLE_XLSX_FILE_PATH, "Sheet1");
        System.out.println(json);
    }

    public static Object processSmallFile(final String filePath, final String sheetName) {
        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
        	return getJsonObject(workbook, sheetName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static Object getJsonObject(Workbook workbook, String sheetName) {
        try {
            Sheet sheet = getSheet(workbook, sheetName);
            List<String> headers = getHeaders(sheet);
            List<Map<String, Object>> data = new ArrayList<>();
            for (Row row : sheet) {
            	List<String> packageDetailsList = new ArrayList<>();
            	List<String> packagePillsList = new ArrayList<>();
            	
                int rowNumber = row.getRowNum();
                if (rowNumber > 0) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    
                    for (int c = 0; c < headers.size(); c++) {
                        Cell cell = row.getCell(c);
                        Object o = getCellValueObject(cell);
                        if(o.toString().equals(""))
                    		continue;
                        String key = headers.get(c);
                        
                        if(key.contains("packageDetail")) {
                        	packageDetailsList.add((String) o);
                        	key = "packageDetails";
                        	o = packageDetailsList;
                        } else if(key.contains("packagePill")) {
                        	packagePillsList.add((String) o);
                        	key = "packagePills";
                        	o = packagePillsList;
                        } 
                        if(o.toString() != "")
                        	rowMap.put(key, o);
                    }
                    data.add(rowMap);
                }
            }
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Sheet getSheet(Workbook workbook, String sheet) {
        int totalSheet = workbook.getNumberOfSheets();
        if (totalSheet == 1) {
            return workbook.getSheetAt(0);
        }
        if (totalSheet > 1) {
            return workbook.getSheet(sheet);
        }
        return null;
    }

    private static List<String> getHeaders(Sheet sheet) {
        List<String> headers = new LinkedList<>();
        for (Row row : sheet) {
            int rowNumber = row.getRowNum();
            if (rowNumber == 0) {
				for (Cell cell : row) { 
					Object cellValue = getCellValueObject(cell);
					headers.add(String.valueOf(cellValue).trim()); 
				}
				 
                break;
            }
        }
        return headers;
    }

    @SuppressWarnings("deprecation")
	private static Object getCellValueObject(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch(cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    Object o = cell.getNumericCellValue();
                    return new BigDecimal(new BigDecimal(String.valueOf(o)).toPlainString());
                }
            default:
                return "";
        }
    }
}
