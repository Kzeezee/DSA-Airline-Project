package org.example.util;

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

    public static void filterNearCommonDominant(
            List<WordCount> goodCounts,
            List<WordCount> badCounts,
            double relativeDifferenceThreshold,
            List<WordCount> outGood,
            List<WordCount> outBad) {

        int i = 0;
        int j = 0;
        while (i < goodCounts.size() && j < badCounts.size()) {
            WordCount gw = goodCounts.get(i);
            WordCount bw = badCounts.get(j);
            int cmp = gw.getWord().compareTo(bw.getWord());
            if (cmp == 0) {
                double p1 = gw.getPercent();
                double p2 = bw.getPercent();
                double relDiff = (Math.max(p1, p2) == 0.0) ? 0.0 : Math.abs(p1 - p2) / Math.max(p1, p2);
                if (relDiff > relativeDifferenceThreshold) {
                    if (p1 > p2) {
                        outGood.add(gw);
                    } else {
                        outBad.add(bw);
                    }
                }
                i++;
                j++;
            } else if (cmp < 0) {
                outGood.add(gw);
                i++;
            } else {
                outBad.add(bw);
                j++;
            }
        }
        while (i < goodCounts.size()) {
            outGood.add(goodCounts.get(i++));
        }
        while (j < badCounts.size()) {
            outBad.add(badCounts.get(j++));
        }
    }

    public static List<WordCount> takeTopK(List<WordCount> counts, int k) {
        ArrayList<WordCount> sorted = new ArrayList<>(counts);
        sorted.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        ArrayList<WordCount> result = new ArrayList<>();
        int limit = Math.min(k, sorted.size());
        for (int idx = 0; idx < limit; idx++) {
            result.add(sorted.get(idx));
        }
        return result;
    }
}

