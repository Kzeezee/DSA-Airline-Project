package org.example.app;

import java.util.*;
import org.example.model.AirlineReview;

public class InteractiveComparison {

    // meaningless tokens which cannot act as attributes are removed using this
    private static final Set<String> EXCLUDED_COMPARISON_WORDS = Set.of(
            "veri", "very",
            "all",
            "class",
            "return",
            "check",
            "board",
            "which",
            "when",
            "would",
            "on",
            "get");

    // Since detokenization was out of scope, I have fixed mapping for some
    // meaningful stems to words.
    private static final Map<String, String> STEM_TO_WORD = Map.ofEntries(
            Map.entry("delai", "delay"),
            Map.entry("fli", "fly"),
            Map.entry("pai", "pay"),
            Map.entry("airlin", "airline"),
            Map.entry("carri", "carry"),
            Map.entry("get", "get"),
            Map.entry("veri", "very"),
            Map.entry("servic", "service"),
            Map.entry("ani", "any"),
            Map.entry("board", "board"),
            Map.entry("cancel", "cancel"),
            Map.entry("tri", "try"),
            Map.entry("arriv", "arrive"),
            Map.entry("told", "tell"),
            Map.entry("ask", "ask"),
            Map.entry("wait", "wait"),
            Map.entry("return", "return"),
            Map.entry("abl", "able"),
            Map.entry("check", "check"),
            Map.entry("call", "call"),
            Map.entry("onli", "only"),
            Map.entry("friendli", "friendly"),
            Map.entry("busi", "business"),
            Map.entry("comfort", "comfort"),
            Map.entry("plane", "plane"),
            Map.entry("food", "food"),
            Map.entry("crew", "crew"),
            Map.entry("staff", "staff"),
            Map.entry("good", "good"),
            Map.entry("class", "class"),
            Map.entry("cabin", "cabin"));

    private static String getReadableWord(String stem) {
        return STEM_TO_WORD.getOrDefault(stem, stem);
    }

    // Converting the word back to stem for analysis and comparison
    private static String getStemFromInput(String userInput) {
        String input = userInput.trim().toLowerCase();

        if (STEM_TO_WORD.containsKey(input)) {
            return input;
        }

        for (Map.Entry<String, String> entry : STEM_TO_WORD.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(input)) {
                return entry.getKey();
            }
        }
        return input;
    }

    // here we are gonna review all the airlines based on the attribute selected.
    public static void run(HashMap<String, List<AirlineReview>> airlineReviews,
            List<AirlineReview> tokenizedReviews) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("  INTERACTIVE AIRLINE TOKEN COMPARISON");
        System.out.println("=".repeat(70));

        System.out.print("\nWould you like to compare airlines based on a specific word? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (!response.equals("yes") && !response.equals("y")) {
            System.out.println("\nThank you for using the Airline Review Analysis System!");
            return;
        }

        System.out.println("\nCompare based on:");
        System.out.println("1. Positive reviews (good words)");
        System.out.println("2. Negative reviews (bad words)");
        System.out.print("Enter choice (1 or 2): ");

        int sentimentChoice = scanner.nextInt();
        scanner.nextLine();

        boolean isPositive = (sentimentChoice == 1);
        String sentimentType = isPositive ? "POSITIVE" : "NEGATIVE";

        List<Map.Entry<String, Integer>> topTokens = getTopCommonTokens(tokenizedReviews, isPositive, 20);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Most common meaningful words from " + sentimentType + " reviews:");
        System.out.println("=".repeat(70));

        for (int i = 0; i < topTokens.size(); i++) {
            Map.Entry<String, Integer> entry = topTokens.get(i);
            String displayWord = getReadableWord(entry.getKey());
            System.out.printf("%2d. %-20s (used %,6d times)\n", i + 1, displayWord, entry.getValue());
        }

        System.out.print("\nEnter the word you want to compare: ");
        String userInput = scanner.nextLine().trim().toLowerCase();

        String chosenToken = getStemFromInput(userInput);

        analyzeTokenAcrossAirlines(airlineReviews, chosenToken, isPositive);
    }

    // getting the most common tokens from the reviews
    private static List<Map.Entry<String, Integer>> getTopCommonTokens(List<AirlineReview> tokenizedReviews,
            boolean isPositive, int topN) {
        Map<String, Integer> tokenFrequency = new HashMap<>();

        for (AirlineReview review : tokenizedReviews) {
            boolean reviewIsPositive = "1".equals(review.getRecommended());
            if (reviewIsPositive == isPositive) {
                for (String token : review.getTokenizedReview()) {
                    tokenFrequency.put(token, tokenFrequency.getOrDefault(token, 0) + 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedTokens = new ArrayList<>(tokenFrequency.entrySet());
        sortedTokens.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Map.Entry<String, Integer>> topNTokens = sortedTokens.subList(0, Math.min(topN, sortedTokens.size()));

        List<Map.Entry<String, Integer>> filteredTokens = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topNTokens) {
            if (!EXCLUDED_COMPARISON_WORDS.contains(entry.getKey())) {
                filteredTokens.add(entry);
            }
        }

        return filteredTokens;
    }

    // Using wrighted score to analyze token across airlines
    private static void analyzeTokenAcrossAirlines(HashMap<String, List<AirlineReview>> airlineReviews,
            String token, boolean isPositive) {

        String sentimentType = isPositive ? "POSITIVE" : "NEGATIVE";
        final int MIN_REVIEWS = 50; // Minimum reviews for reliable stats

        class AirlineTokenStats implements Comparable<AirlineTokenStats> {
            String airline;
            int tokenCount;
            int totalReviews;
            double percentage;
            double weightedScore;

            AirlineTokenStats(String airline, int tokenCount, int totalReviews) {
                this.airline = airline;
                this.tokenCount = tokenCount;
                this.totalReviews = totalReviews;
                this.percentage = totalReviews > 0 ? (tokenCount * 100.0 / totalReviews) : 0;

                this.weightedScore = totalReviews >= MIN_REVIEWS
                        ? percentage * Math.log10(totalReviews + 1)
                        : percentage * Math.log10(totalReviews + 1) * 0.5; // Penalty for low review count
            }

            @Override
            public int compareTo(AirlineTokenStats other) {
                return Double.compare(other.weightedScore, this.weightedScore);
            }
        }

        List<AirlineTokenStats> stats = new ArrayList<>();

        for (Map.Entry<String, List<AirlineReview>> entry : airlineReviews.entrySet()) {
            String airline = entry.getKey();
            List<AirlineReview> reviews = entry.getValue();

            int tokenCount = 0;
            int sentimentReviewCount = 0;

            for (AirlineReview review : reviews) {
                boolean reviewIsPositive = "1".equals(review.getRecommended());
                if (reviewIsPositive == isPositive) {
                    sentimentReviewCount++;
                    for (String reviewToken : review.getTokenizedReview()) {
                        if (reviewToken.equals(token)) {
                            tokenCount++;
                        }
                    }
                }
            }

            if (tokenCount > 0) {
                stats.add(new AirlineTokenStats(airline, tokenCount, sentimentReviewCount));
            }
        }

        Collections.sort(stats);

        String displayWord = getReadableWord(token);
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("  COMPARISON RESULTS FOR: \"" + displayWord.toUpperCase() + "\"");
        System.out.println("  Sentiment: " + sentimentType + " reviews");
        System.out.println("  Total airlines with this word: " + stats.size());
        System.out.println("  Minimum reviews for reliability: " + MIN_REVIEWS);
        System.out.println("=".repeat(70));

        System.out.println("\n" + "=".repeat(70));
        System.out.println("TOP 5 Airlines with HIGHEST weighted score for \"" + displayWord + "\":");
        System.out.println("(Weighted score considers both frequency % and review count)");
        System.out.println("=".repeat(70));
        System.out.println(String.format("%-4s %-25s %10s %10s %10s %12s",
                "Rank", "Airline", "Reviews", "Count", "Percent", "Score"));
        System.out.println("-".repeat(70));

        int top5Count = Math.min(5, stats.size());
        for (int i = 0; i < top5Count; i++) {
            AirlineTokenStats stat = stats.get(i);
            String reliable = stat.totalReviews >= MIN_REVIEWS ? "" : "*";
            System.out.printf("%-4d %-25s %10d %10d %9.2f%% %11.2f %s\n",
                    i + 1, stat.airline, stat.totalReviews, stat.tokenCount,
                    stat.percentage, stat.weightedScore, reliable);
        }
    }
}
