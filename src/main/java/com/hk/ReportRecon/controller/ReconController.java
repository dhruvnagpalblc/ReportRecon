package com.hk.ReportRecon.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("recon")
@Slf4j
public class ReconController {

    private final WordVectors word2Vec;
    private static final String WORD2VEC_MODEL_PATH = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/GoogleNews-vectors-negative300.bin";
    private static final String excel1Path = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/excel1.xlsx";
    private static final String excel2Path = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/excel2.xlsx";

    ReconController () {
        word2Vec = WordVectorSerializer.readWord2VecModel(WORD2VEC_MODEL_PATH);
    }

    @GetMapping("/test2")
    public ResponseEntity<byte[]> test2() {
        // read 2 excel
        try (FileInputStream fis1 = new FileInputStream(excel1Path);
             FileInputStream fis2 = new FileInputStream(excel2Path);
             Workbook workbook1 = WorkbookFactory.create(fis1);
             Workbook workbook2 = WorkbookFactory.create(fis2);
             Workbook workbook3 = new XSSFWorkbook()) {

            Sheet sheet1 = workbook1.getSheetAt(0);
            Sheet sheet2 = workbook2.getSheetAt(0);

            List<String> excel1Headers = new ArrayList<>();
            List<String> excel2Headers = new ArrayList<>();

            evaluateHeaders(sheet1, excel1Headers, sheet2, excel2Headers);

            // make similar header map
            Map<Integer, Integer> header1Header2IntMap = new HashMap<>();
            Map<String, String> header1Header2StringMap = new HashMap<>();
            evaluateSimilarHeaderMap(excel1Headers, excel2Headers, sheet1, sheet2, header1Header2IntMap, header1Header2StringMap);

            // create 3rd excel
            Sheet sheet3 = workbook3.createSheet();

            createHeadersFor3rdExcel(sheet3, header1Header2StringMap, sheet1);

            addEntriesIn3rdExcel(header1Header2IntMap, sheet1, sheet3, sheet2, workbook3);

            autoSizeRowsAndColumn(sheet3);

            // Write the workbook to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook3.write(outputStream);

            // Set the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "excel3.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/test3")
    public Map<String, String> test3(@RequestParam("file1") MultipartFile excelFile1,
                                        @RequestParam("file2") MultipartFile excelFile2) {
        try (Workbook workbook1 = WorkbookFactory.create(excelFile1.getInputStream());
             Workbook workbook2 = WorkbookFactory.create(excelFile2.getInputStream());
             Workbook workbook3 = new XSSFWorkbook()) {

            Sheet sheet1 = workbook1.getSheetAt(0);
            Sheet sheet2 = workbook2.getSheetAt(0);

            List<String> excel1Headers = new ArrayList<>();
            List<String> excel2Headers = new ArrayList<>();

            evaluateHeaders(sheet1, excel1Headers, sheet2, excel2Headers);

            // make similar header map
            Map<Integer, Integer> header1Header2IntMap = new HashMap<>();
            Map<String, String> header1Header2StringMap = new HashMap<>();
            evaluateSimilarHeaderMap(excel1Headers, excel2Headers, sheet1, sheet2, header1Header2IntMap, header1Header2StringMap);

            return header1Header2StringMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addEntriesIn3rdExcel(Map<Integer, Integer> header1Header2IntMap, Sheet sheet1, Sheet sheet3, Sheet sheet2, Workbook workbook3) {
        // add entries in 3rd excel
        for (Map.Entry<Integer, Integer> entry : header1Header2IntMap.entrySet()) {
            for (int rowIndex = 1; rowIndex <= sheet1.getLastRowNum(); rowIndex++) {
                int excel1ColumnIndex = entry.getKey();
                int excel2ColumnIndex = entry.getValue();
                Row sheet3Row = sheet3.getRow(rowIndex);
                if (sheet3Row == null) {
                    sheet3Row = sheet3.createRow(rowIndex);
                }
                DataFormatter dataFormatter = new DataFormatter();
                Cell cell1 = sheet1.getRow(rowIndex).getCell(excel1ColumnIndex);
                Cell cell2 = sheet2.getRow(rowIndex).getCell(excel2ColumnIndex);
                String value1 = dataFormatter.formatCellValue(cell1);
                String value2 = dataFormatter.formatCellValue(cell2);
                Cell sheet3RowCell = sheet3Row.createCell(excel1ColumnIndex);
                sheet3RowCell.setCellValue(value1 + "\n" + value2);

                CellStyle style3 = workbook3.createCellStyle();
                Font font3 = workbook3.createFont();
                if (value1.equals(value2)) {
                    font3.setColor(IndexedColors.GREEN.getIndex());
                } else {
                    font3.setColor(IndexedColors.RED.getIndex());
                }
                style3.setFont(font3);
                sheet3RowCell.setCellStyle(style3);
            }
        }
    }

    private static void createHeadersFor3rdExcel(Sheet sheet3, Map<String, String> header1Header2StringMap, Sheet sheet1) {
        // create 3rd excel headers
        Row sheet3HeaderRow = sheet3.createRow(0);
        for (Map.Entry<String, String> entry : header1Header2StringMap.entrySet()) {
            int columnNumber = getColumnNumber(sheet1.getRow(0), entry.getKey());
            sheet3HeaderRow.createCell(columnNumber).setCellValue(entry.getKey() + "\n" + entry.getValue());
        }
    }

    private static void evaluateHeaders(Sheet sheet1, List<String> excel1Headers, Sheet sheet2, List<String> excel2Headers) {
        // collect header info
        sheet1.getRow(0).forEach(
                cell -> excel1Headers.add(cell.getStringCellValue().trim().toLowerCase())
        );
        sheet2.getRow(0).forEach(
                cell -> excel2Headers.add(cell.getStringCellValue().trim().toLowerCase())
        );
    }

    private void evaluateSimilarHeaderMap(List<String> excel1Headers, List<String> excel2Headers, Sheet sheet1, Sheet sheet2, Map<Integer, Integer> header1Header2IntMap, Map<String, String> header1Header2StringMap) {
        for (String header1 : excel1Headers) {
            double maxSimilarity = 0;
            String maxSimilarityHeader = null;
            for (String header2 : excel2Headers) {
                if (header1.equals(header2)
                        || convertToShortForm(header1).equals(convertToShortForm(header2))) {
                    maxSimilarityHeader = header2;
                    break;
                }
                double currentSimilarity = word2Vec.similarity(header1, header2);
                if (currentSimilarity > maxSimilarity) {
                    maxSimilarity = currentSimilarity;
                    maxSimilarityHeader = header2;
                }
            }
            if (maxSimilarityHeader != null) {
                int columnNumber1 = getColumnNumber(sheet1.getRow(0), header1);
                int columnNumber2 = getColumnNumber(sheet2.getRow(0), maxSimilarityHeader);
                header1Header2IntMap.put(columnNumber1, columnNumber2);
                header1Header2StringMap.put(header1, maxSimilarityHeader);
            }
        }
    }

    public String convertToShortForm(String input) {
        input = input.replaceAll("\\s", "").replaceAll("[aeioun]", "");
        return input
                .toLowerCase()
                .chars()
                .distinct()
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public static int getColumnNumber(Row headerRow, String headerToFind) {
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        int columnNumber = 0;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().toLowerCase().equals(headerToFind)) {
                return columnNumber;
            }
            columnNumber++;
        }
        return -1;
    }

    public static void autoSizeRowsAndColumn(Sheet sheet) {
        for (Row row : sheet) {
            row.setHeight((short) 700);
        }

        for (int columnIndex = 0; columnIndex < sheet.getRow(0).getLastCellNum(); columnIndex++) {
            sheet.setColumnWidth(columnIndex, 2000);
        }
    }
}
