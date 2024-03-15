package com.hk.ReportRecon.controller;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("train")
@Slf4j
public class TrainModel {
    private static final String FILE_PATH = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/corpus.txt";
    private static final String WORD_2_VEC_FILE = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/GoogleNews-vectors-negative300Copy.bin";

    @GetMapping("/train1")
    public void train1() {

        try {
            SentenceIterator iter = new BasicLineIterator( new File(FILE_PATH) );

            // Step 2: Configure and train the Word2Vec model
            TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
            tokenizerFactory.setTokenPreProcessor( new CommonPreprocessor() );

            int vectorSize = 100;
            int windowSize = 5;
            int minWordFrequency = 5;

            while (iter.hasNext()) {
                String sentence = iter.nextSentence();
                tokenizerFactory.create(sentence);
            }

            iter.reset();

            Word2Vec vec = new Word2Vec.Builder()
                    .minWordFrequency( minWordFrequency )
                    .iterations( 5 )
                    .layerSize( vectorSize )
                    .seed( 42 )
                    .windowSize( windowSize )
                    .iterate( iter )
                    .tokenizerFactory( tokenizerFactory )
                    .build();

            vec.fit();

            // Step 3: Save the model (optional)
            WordVectorSerializer.writeWord2VecModel( vec, WORD_2_VEC_FILE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
