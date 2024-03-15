package com.hk.ReportRecon.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class JaccardSimilarityService {

    public double calculateJaccardSimilarity(String str1, String str2) {
        Set<Character> set1 = tokenizeString(str1);
        Set<Character> set2 = tokenizeString(str2);

        return calculateJaccardSimilarity(set1, set2);
    }

    private Set<Character> tokenizeString(String str) {
        Set<Character> set = new HashSet<>();
        for (char c : str.toCharArray()) {
            set.add(c);
        }
        return set;
    }

    private double calculateJaccardSimilarity(Set<Character> set1, Set<Character> set2) {
        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);

        double intersectionSize = intersection.size();
        double unionSize = union.size();

        if (unionSize == 0) {
            return 0; // Jaccard similarity is undefined if both sets are empty
        }

        return intersectionSize / unionSize;
    }
}
