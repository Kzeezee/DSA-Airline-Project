package org.example.ds.arraylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.example.model.AirlineReview;
import org.example.model.WordCount;
import org.example.util.Pair;
import org.example.util.TextAnalysisUtils;

public class AirlineArrayListImpl {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineArrayListImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<ArrayList<WordCount>, ArrayList<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }
        ArrayList<String> goodWords = new ArrayList<>();
        ArrayList<String> badWords = new ArrayList<>();

        TextAnalysisUtils.accumulateWordsByRecommendation(reviews, goodWords, badWords);

        List<WordCount> goodCounts = compressCounts(goodWords);
        List<WordCount> badCounts = compressCounts(badWords);

        ArrayList<WordCount> filteredGood = new ArrayList<>();
        ArrayList<WordCount> filteredBad = new ArrayList<>();
        TextAnalysisUtils.filterNearCommonDominant(goodCounts, badCounts, 0.30, filteredGood, filteredBad);

        List<WordCount> topGood = TextAnalysisUtils.takeTopK(filteredGood, 10);
        List<WordCount> topBad = TextAnalysisUtils.takeTopK(filteredBad, 10);

        return Pair.of(new ArrayList<>(topGood), new ArrayList<>(topBad));
    }

    private static List<WordCount> compressCounts(List<String> words) {
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

