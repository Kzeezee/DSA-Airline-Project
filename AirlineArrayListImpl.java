import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class AirlineArrayListImpl {
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

        ArrayList<WordCount> filteredGood = new ArrayList<>();
        ArrayList<WordCount> filteredBad = new ArrayList<>();
        TextAnalysisUtils.filterNearCommonDominant(goodCounts, badCounts, 0.30, filteredGood, filteredBad);

        List<WordCount> topGood = TextAnalysisUtils.takeTopK(filteredGood, 10);
        List<WordCount> topBad = TextAnalysisUtils.takeTopK(filteredBad, 10);

        return Pair.<List<WordCount>, List<WordCount>>of(topGood, topBad);
    }

    
}

 

 