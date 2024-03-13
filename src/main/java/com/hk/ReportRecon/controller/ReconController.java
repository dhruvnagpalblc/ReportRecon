package com.hk.ReportRecon.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
//
//    @GetMapping("/test1")
//    public double test1() {
//        double similarity = 0;
//        try {
//            String word1 = "awb";
//            String word2 = "awb";
//            similarity = word2Vec.similarity(word1, word2);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return similarity;
//    }

    @GetMapping("/test2")
    public String test2() {
        Map<String, String> header1Header2Map = new HashMap<>();
        try (FileInputStream fis1 = new FileInputStream(excel1Path);
             FileInputStream fis2 = new FileInputStream(excel2Path);
             Workbook workbook1 = WorkbookFactory.create(fis1);
             Workbook workbook2 = WorkbookFactory.create(fis2)) {

            Sheet sheet1 = workbook1.getSheetAt(0);
            Sheet sheet2 = workbook2.getSheetAt(0);

            List<String> excel1Headers = new ArrayList<>();
            List<String> excel2Headers = new ArrayList<>();

            sheet1.getRow(0).forEach(
                    cell -> excel1Headers.add(cell.getStringCellValue().trim().toLowerCase())
            );
            sheet2.getRow(0).forEach(
                    cell -> excel2Headers.add(cell.getStringCellValue().trim().toLowerCase())
            );

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
                    header1Header2Map.put(header1, maxSimilarityHeader);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return header1Header2Map.toString();
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
}
