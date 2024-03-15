package com.hk.ReportRecon.controller;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
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
@RequestMapping("recon")
@Slf4j
public class TrainModel {

    @GetMapping("/train1")
    public void train1() {

        try {

            String filePath = "/home/dhruv.nagpal@Brightlifecare.local/Downloads/corpus.txt";
            SentenceIterator iter = new BasicLineIterator( new File( filePath ) );

            // Step 2: Configure and train the Word2Vec model
            TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
            tokenizerFactory.setTokenPreProcessor( new CommonPreprocessor() );

            int vectorSize = 100;
            int windowSize = 5;
            int minWordFrequency = 5;

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
            WordVectorSerializer.writeWord2VecModel( vec, "/home/dhruv.nagpal@Brightlifecare.local/Downloads/GoogleNews-vectors-negative300Copy.bin" );

            // Step 4: Use the trained model to find similarities
            WordVectors wordVectors = WordVectorSerializer.loadStaticModel( new File( "path/to/your/model.bin" ) );

            String word1 = "example";
            String word2 = "demonstration";

            double similarity = wordVectors.similarity( word1, word2 );
            System.out.println( "Similarity between '" + word1 + "' and '" + word2 + "': " + similarity );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
