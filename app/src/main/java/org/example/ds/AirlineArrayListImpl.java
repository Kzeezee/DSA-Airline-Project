package org.example.ds;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.TextAnalysisUtils;
import org.example.model.util.WordCount;

public class AirlineArrayListImpl implements WordFrequencyAnalyzer {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineArrayListImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }
        ArrayList<String> goodWords = new ArrayList<>();
        ArrayList<String> badWords = new ArrayList<>();

        TextAnalysisUtils.accumulateWordsByRecommendation(reviews, goodWords, badWords);

        List<WordCount> goodCounts = TextAnalysisUtils.compressCounts(goodWords);
        List<WordCount> badCounts = TextAnalysisUtils.compressCounts(badWords);

        List<WordCount> topGood = topKFromArrayList(goodCounts, 10);
        List<WordCount> topBad = topKFromArrayList(badCounts, 10);

        return Pair.<List<WordCount>, List<WordCount>>of(topGood, topBad);
    }

    private List<WordCount> topKFromArrayList(List<WordCount> counts, int k) {
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