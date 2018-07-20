package com.portal.presentation;
import java.io.*
import java.text.*;
import java.util.*;
import org.apache.poi.ss.*;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import com.monitorjbl.xlsx.*;

public class FileUploadWizard {

	private String destination = "\\\\jaxdfscl001\\it\\Projects\\Salman\\";

/**
	 * File Upload Module starts
	 * 
	 * @throws Exception
	 **/

	public void handleFileUpload(FileUploadEvent event) throws Exception {
		// Do what you want with the file
		try {
			UploadedFile file = event.getFile();
			String fileName = file.getFileName();
			copyFile(destination, fileName, file.getInputstream());
			if (fileName.endsWith(".CSV") || fileName.endsWith(".csv")) {
				displayCSV(destination, fileName);
			} else if (fileName.endsWith(".XLSX") || fileName.endsWith(".xlsx") || fileName.endsWith(".xls")
					|| fileName.endsWith(".XLS")) {
				displayExcelNew(destination, fileName);

			}
			FacesMessage msg = new FacesMessage("Success! ", event.getFile().getFileName() + " is uploaded.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void copyFile(String destination, String fileName, InputStream in) {
		try {

			long startTime = System.currentTimeMillis();
			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(new File(destination + fileName));

			int read = 0;
			byte[] bytes = new byte[4096];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			in.close();
			out.flush();
			out.close();
			long endTime = System.currentTimeMillis();

			System.out.println("File Successfully Upladed! Time: " + ((endTime - startTime) / 1000));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/** File Upload Module ends **/

/** Read Excel Header and return Data Types - Only String, Int, Date**/

//Streaming Method for large records (Excel Files) 
	public void displayExcelNew(String destination, String fileName) throws Exception {

		Workbook excelWorkbook = StreamingReader.builder().rowCacheSize(2).open(new File(destination + fileName));
		Sheet excelSheet = excelWorkbook.getSheetAt(0);
		Row excelSheetRow = excelSheet.rowIterator().next();
		for (Cell excelHeaderCell : excelSheetRow) {
			System.out.print(excelHeaderCell.getStringCellValue() + "|");
		}
		System.out.println();
		excelSheetRow = excelSheet.rowIterator().next();
		int columnCount = excelSheetRow.getLastCellNum();
		Cell excelSecondRowCell;

		String setColtype = "";
		for (int i = 0; i < columnCount; i++) {
			excelSecondRowCell = excelSheetRow.getCell(i);
			if (excelSecondRowCell == null) {
				System.out.println('~');
				setColtype = "String";
				System.out.print(setColtype + ",");

			} else {

				switch (excelSecondRowCell.getCellTypeEnum()) {
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(excelSecondRowCell)) {
						setColtype = "Date";
					} else {
						setColtype = "Integer";
					}
					break;
				case STRING:
					setColtype = "String";
					break;
				default:
					setColtype = "String";
					break;
				}
				System.out.print(setColtype + ",");
			}
		}
		excelWorkbook.close();
	}

	/** CSV File Parsing Header Start **/

	public void displayCSV(String destination, String fileName) throws Exception {
		BufferedReader brFileReader = new BufferedReader(new FileReader(destination + fileName));
		String csvheader = brFileReader.readLine();
		// Stop. header is the first line.
		System.out.println(csvheader);
		/*
		 * String[] strArray = header.split(",");
		 * System.out.println(Arrays.toString(strArray));
		 */
		String nextLine = brFileReader.readLine();
		List<String> items = Arrays.asList(nextLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
		String setDataType = null;

		List<String> dateknownPatterns = new ArrayList<>();
		dateknownPatterns.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateknownPatterns.add("yyyy-MM-dd'T'HH:mm.ss'Z'");
		dateknownPatterns.add("yyyy-MM-dd'T'HH:mm:ss");
		dateknownPatterns.add("yyyy-MM-dd' 'HH:mm:ss");
		dateknownPatterns.add("yyyy-MM-dd'T'HH:mm:ssXXX");
		dateknownPatterns.add("mm/dd/yyyy HH:mm");
		dateknownPatterns.add("mm/dd/yyyy");
		dateknownPatterns.add("yyyy-MM-dd");
		for (String csvList : items) {
			try {
				Integer.parseInt(csvList);
				setDataType = "Integer";
			} catch (NumberFormatException numEx) {
				for (String pattern : dateknownPatterns) {
					try {
						new SimpleDateFormat(pattern).parse(csvList.trim());
						setDataType = "Date";
						break;
					} catch (ParseException pe) {
						setDataType = "String";
					}
				}
				if (setDataType == null) {
					setDataType = "String";
				}
			}
			System.out.print(setDataType + ",");
		}
		System.out.println();
		brFileReader.close();

	}

	/** CSV File Parsing Header Ends **/


	/** Excel - default method**/

	public void displayExcel(String destination, String fileName) throws Exception {
		long startTime = System.currentTimeMillis();
		Workbook workbook = new XSSFWorkbook(destination + fileName);
		long endTime = System.currentTimeMillis();
		System.out.println("Time taken to open workbook: " + ((endTime - startTime) / 1000));
		// Workbook workbook = new XSSFWorkbook(new File(destination + fileName));
		Sheet sheet = workbook.getSheetAt(0);
		Row headerRow = sheet.getRow(0);

		// List of headers from excel
		List<String> headers = new ArrayList<>();
		Iterator<Cell> cells = headerRow.cellIterator();
		while (cells.hasNext()) {
			Cell cell = cells.next();
			RichTextString value = cell.getRichStringCellValue();
			headers.add(value.getString());
		}

		workbook.close();

		for (String e : headers) {
			System.out.println(e);
		}
	}
  public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	
	}
}
