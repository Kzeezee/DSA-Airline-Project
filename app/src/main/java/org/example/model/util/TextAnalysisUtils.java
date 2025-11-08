package org.example.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.example.model.AirlineReview;

public class TextAnalysisUtils {
    public static boolean isPositiveRecommendation(String recommended) {
        return recommended != null && recommended.replace("\"", "").equals("1");
    }

    public static void accumulateWordsByRecommendation(
            List<AirlineReview> reviews,
            List<String> goodWords,
            List<String> badWords) {
        for (AirlineReview review : reviews) {
            if (isPositiveRecommendation(review.getRecommended())) {
                goodWords.addAll(Arrays.asList(review.getTokenizedReview()));
            } else {
                badWords.addAll(Arrays.asList(review.getTokenizedReview()));
            }
        }
    }

    public static List<WordCount> compressCounts(List<String> words) {
        ArrayList<WordCount> counts = new ArrayList<>();
        if (words.isEmpty()) {
            return counts;
        }
        ArrayList<String> sorted = new ArrayList<>(words);
        Collections.sort(sorted);
        int total = sorted.size();
        String currentWord = sorted.get(0);
        int runLength = 1;
        for (int i = 1; i < sorted.size(); i++) {
            String word = sorted.get(i);
            if (word.equals(currentWord)) {
                runLength++;
            } else {
                counts.add(new WordCount(currentWord, runLength, total));
                currentWord = word;
                runLength = 1;
            }
        }
        counts.add(new WordCount(currentWord, runLength, total));
        return counts;
    }
}

