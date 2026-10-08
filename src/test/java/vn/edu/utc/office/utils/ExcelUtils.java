package vn.edu.utc.office.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ExcelUtils provides utility methods to read and write test data from/to Excel files (.xlsx)
 * using the Apache POI library. Supports Data-Driven Testing workflows.
 */
public class ExcelUtils {

    private String filePath;
    private Workbook workbook;
    private Sheet sheet;

    /**
     * Initializes the Excel reader with a target file path and sheet name.
     *
     * @param filePath  Absolute or relative path to the .xlsx file
     * @param sheetName Name of the sheet to inspect
     * @throws IOException If file reading fails
     */
    public ExcelUtils(String filePath, String sheetName) throws IOException {
        this.filePath = filePath;
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
        this.workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(bytes));
        this.sheet = workbook.getSheet(sheetName);
        if (this.sheet == null) {
            throw harvestSheetNotFoundError(sheetName);
        }
    }

    private IllegalArgumentException harvestSheetNotFoundError(String sheetName) {
        return new IllegalArgumentException("Sheet '" + sheetName + "' does not exist in workbook: " + filePath);
    }

    /**
     * Retrieves the total number of physical rows present in the sheet.
     *
     * @return Row count
     */
    public int getRowCount() {
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * Reads all test case records (excluding the header row) as a 2D Object array
     * compatible with TestNG @DataProvider.
     *
     * Expected column order:
     * [0] Test Case ID, [1] Scenario, [2] Username, [3] Password, [4] Expected Result
     *
     * @return Two-dimensional object array containing test case datasets
     */
    public Object[][] getTestDataForDataProvider() {
        int totalRows = sheet.getLastRowNum(); // 0-indexed; row 0 is header
        List<Object[]> dataList = new ArrayList<>();

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            String testCaseId = getCellValueAsString(row.getCell(0));
            String scenario = getCellValueAsString(row.getCell(1));
            String username = getCellValueAsString(row.getCell(2));
            String password = getCellValueAsString(row.getCell(3));
            String expectedResult = getCellValueAsString(row.getCell(4));

            dataList.add(new Object[]{i, testCaseId, scenario, username, password, expectedResult});
        }

        Object[][] dataArray = new Object[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            dataArray[i] = dataList.get(i);
        }
        return dataArray;
    }

    /**
     * Retrieves test case datasets as JUnit 5 Arguments stream for @ParameterizedTest.
     */
    public java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> getArgumentsStream() {
        Object[][] data = getTestDataForDataProvider();
        List<org.junit.jupiter.params.provider.Arguments> args = new ArrayList<>();
        for (Object[] row : data) {
            args.add(org.junit.jupiter.params.provider.Arguments.of(row));
        }
        return args.stream();
    }

    /**
     * Records execution results into memory for the target row.
     *
     * @param rowIndex     Index of the row to update (1-based for data rows)
     * @param actualResult The actual observed result during test execution
     * @param status       Execution status ('PASS', 'FAIL', 'SKIPPED')
     */
    public synchronized void writeResult(int rowIndex, String actualResult, String status) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        // Column 5: Actual Result, Column 6: Status
        Cell actualCell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        actualCell.setCellValue(actualResult);

        Cell statusCell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        statusCell.setCellValue(status);

        // Apply visual styling (Green for PASS, Red for FAIL)
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);

        if ("PASS".equalsIgnoreCase(status)) {
            font.setColor(IndexedColors.GREEN.getIndex());
        } else if ("FAIL".equalsIgnoreCase(status)) {
            font.setColor(IndexedColors.RED.getIndex());
        }
        style.setFont(font);
        statusCell.setCellStyle(style);
    }

    /**
     * Persists all recorded updates back into the physical Excel spreadsheet.
     *
     * @throws IOException If writing to the file fails
     */
    public synchronized void save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
    }

    /**
     * Safely extracts cell value as String regardless of underlying cell type.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
