package br.com.mapfre.lv.pocs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class TesteUserModelXLS {

	public static void main(String[] args) throws IOException {

		InputStream is = new FileInputStream("planilhaGigante2.xlsx");

		Workbook workBook = null;
		try {
			workBook = WorkbookFactory.create(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Sheet sheet = workBook.getSheetAt(0);

		for (int i = 0; i <= sheet.getLastRowNum(); i++) {

			Row row = sheet.getRow(i);

			for (int j = 0; j < row.getLastCellNum(); j++) {

				Cell cell = row.getCell(j);

				String parseCellValue = parseCellValue(workBook, cell);

				System.out.println("val:" +parseCellValue);
			}

		}
	}

	private static String parseCellValue(Workbook workBook, Cell cell) {
		FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();

		String cellValue = null;
		if (cell != null) {
		
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				cellValue = cell.getRichStringCellValue().getString();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					cellValue = cell.getDateCellValue().toString();
				} else {
					cellValue = new Double(cell.getNumericCellValue())
							.toString();
				}
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cellValue = new Boolean(cell.getBooleanCellValue()).toString();
				break;
			case Cell.CELL_TYPE_FORMULA:
				cellValue = evaluator.evaluate(cell).formatAsString();
				break;
			}
		}
		return cellValue;
	}

}
