package com.hk.ReportRecon.controller;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("recon")
@Slf4j
public class ReconController {

    private final WordVectors word2Vec;
    private static final String WORD2VEC_MODEL_PATH = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/GoogleNews-vectors-negative300.bin";

    ReconController () {
        word2Vec = WordVectorSerializer.readWord2VecModel(WORD2VEC_MODEL_PATH);
    }

    @GetMapping("/test1")
    public double test1() {
        double similarity = 0;
        try {
            String word1 = "Amount";
            String word2 = "Price";
            similarity = word2Vec.similarity(word1, word2);
            similarity = word2Vec.similarity("Quantity", "Unit");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return similarity;
    }
}
