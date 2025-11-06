package org.example.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.en.PorterStemFilter;

import org.example.ds.AirlineArrayListImpl;
import org.example.ds.AirlineBSTImpl;
import org.example.ds.AirlineRBTreeImpl;
import org.example.ds.AirlineMapImpl;
import org.example.ds.WordFrequencyAnalyzer;
import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.WordCount;

/*
 * Combined Implementation - Shows both BST and Priority Queue approaches
 * Index mappings from CSV:
 * Index 0  - Airlines
 * Index 6  - Review content
 * Index 11 - Overall rating
 * Index 19 - Recommended
 */

public class Main {
    public static Set<String> uselessWord = Set.of("i", "you", "we", "my", "were", "have", "had",
            "us", "our", "so", "from");

    /**
     * Words to exclude from interactive comparison - too generic/meaningless as
     * comparison attributes
     */
    private static final Set<String> EXCLUDED_COMPARISON_WORDS = Set.of(
            "veri", "very", // Too generic
            "all", // Too generic
            "class", // Ambiguous (class of service vs. word "class")
            "return", // Ambiguous (return flight vs. verb)
            "check", // Too generic (check-in, check baggage, etc.)
            "board", // Too generic
            "which", // Grammatical word
            "when", // Grammatical word
            "would", // Grammatical word
            "on", // Preposition
            "get" // Too generic
    );

    /**
     * Common stem to readable word mapping for better display
     * Only maps the most common/confusing stems that appear in top 20
     */
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
            // Additional common stems from negative reviews
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

    /**
     * Convert a stemmed token to a more readable word for display
     */
    private static String getReadableWord(String stem) {
        return STEM_TO_WORD.getOrDefault(stem, stem);
    }

    /**
     * Convert user input (readable word) back to the stem for searching
     * Handles both stem input and readable word input
     */
    private static String getStemFromInput(String userInput) {
        String input = userInput.trim().toLowerCase();

        // First check if input is already a stem (direct match in keys)
        if (STEM_TO_WORD.containsKey(input)) {
            return input;
        }

        // Otherwise, search for the stem that maps to this readable word
        for (Map.Entry<String, String> entry : STEM_TO_WORD.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(input)) {
                return entry.getKey();
            }
        }

        // If not found in mapping, return the input as-is (might be a word without
        // mapping)
        return input;
    }

    /**
     * Parse CSV line handling quoted fields with commas
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result.toArray(new String[0]);
    }

    private static void runAnalyzer(String title, WordFrequencyAnalyzer analyzer) {
        System.out.println("\n========================================");
        System.out.println("      " + title + " Implementation Test");
        System.out.println("========================================");

        Pair<List<WordCount>, List<WordCount>> top10 = analyzer.getTop10MostCommonWords();

        System.out.println("\n[GOOD] Top 10 most common words in POSITIVE reviews:");
        System.out.println("-----------------------------------------");
        int goodRank = 1;
        for (WordCount wc : top10.getLeft()) {
            System.out.printf("%2d. Word: %-15s Count: %5d\n",
                    goodRank++, wc.getWord(), wc.getCount());
        }

        System.out.println("\n[BAD] Top 10 most common words in NEGATIVE reviews:");
        System.out.println("-----------------------------------------");
        int badRank = 1;
        for (WordCount wc : top10.getRight()) {
            System.out.printf("%2d. Word: %-15s Count: %5d\n",
                    badRank++, wc.getWord(), wc.getCount());
        }
    }

    public static void main(String[] args) throws IOException {
        List<String[]> records = new ArrayList<>();

        // Read CSV from resources folder
        InputStream csvStream = Main.class.getClassLoader().getResourceAsStream("airline.csv");

        if (csvStream == null) {
            System.out.println("ERROR: Could not find airline.csv in resources folder!");
            return;
        }

        System.out.println("✓ Found airline.csv in resources");

        // Read values from CSV
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream))) {
            String line = br.readLine(); // Skip header
            StringBuilder currentLine = new StringBuilder();
            boolean inQuotes = false;

            while ((line = br.readLine()) != null) {
                currentLine.append(line);

                // Check if we're inside a quoted field
                for (char c : line.toCharArray()) {
                    if (c == '"')
                        inQuotes = !inQuotes;
                }

                // If still in quotes, this is a multi-line field
                if (inQuotes) {
                    currentLine.append("\n");
                    continue;
                }

                // Parse the complete line
                String[] row = parseCSVLine(currentLine.toString());
                String[] selectedValues = new String[4];

                selectedValues[0] = row.length > 0 ? (row[0].isEmpty() ? null : row[0]) : null;
                selectedValues[1] = row.length > 6 ? (row[6].isEmpty() ? null : row[6]) : null;
                selectedValues[2] = row.length > 11 ? (row[11].isEmpty() ? null : row[11]) : null;
                selectedValues[3] = row.length > 19 ? (row[19].isEmpty() ? null : row[19]) : null;

                records.add(selectedValues);
                currentLine = new StringBuilder();
            }
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // Tokenize reviews
        List<AirlineReview> tokenizedReviews = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet())) {
            int i = 0;
            for (String[] review : records) {
                if (review[1] != null) {
                    List<String> tokens = new ArrayList<>();

                    try (TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(review[1]));
                            TokenStream stemmedStream = new PorterStemFilter(tokenStream)) {
                        CharTermAttribute attr = stemmedStream.addAttribute(CharTermAttribute.class);
                        stemmedStream.reset();

                        // Collect all stemmed tokens, filtering out useless words
                        while (stemmedStream.incrementToken()) {
                            if (!uselessWord.contains(attr.toString())) {
                                tokens.add(attr.toString());
                            }
                        }
                        stemmedStream.end();
                    }

                    if (!tokens.isEmpty()) {
                        tokenizedReviews.add(new AirlineReview(
                                records.get(i)[0],
                                tokens.toArray(new String[0]),
                                records.get(i)[2],
                                records.get(i)[3]));
                    }
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error processing reviews: " + e.getMessage());
            e.printStackTrace();
        }

        // Group reviews by airline
        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();
        for (AirlineReview airlineReview : tokenizedReviews) {
            if (!airlineReviews.containsKey(airlineReview.getAirline())) {
                airlineReviews.put(airlineReview.getAirline(), new ArrayList<>());
            }
            airlineReviews.get(airlineReview.getAirline()).add(airlineReview);
        }

        // Unified test harness for all 4 implementations
        String testAirline = "spirit-airlines";
        runAnalyzer("BST", new AirlineBSTImpl(airlineReviews, testAirline));
        runAnalyzer("Red-Black Tree", new AirlineRBTreeImpl(airlineReviews, testAirline));
        runAnalyzer("ArrayList", new AirlineArrayListImpl(airlineReviews, testAirline));
        runAnalyzer("Map", new AirlineMapImpl(airlineReviews, testAirline));

        // Interactive token comparison feature
        interactiveTokenComparison(airlineReviews, tokenizedReviews);
    }

    /**
     * Interactive feature to compare airlines based on a specific token
     */
    private static void interactiveTokenComparison(HashMap<String, List<AirlineReview>> airlineReviews,
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

        // Step 1: Choose sentiment (good or bad reviews)
        System.out.println("\nCompare based on:");
        System.out.println("1. Positive reviews (good words)");
        System.out.println("2. Negative reviews (bad words)");
        System.out.print("Enter choice (1 or 2): ");

        int sentimentChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        boolean isPositive = (sentimentChoice == 1);
        String sentimentType = isPositive ? "POSITIVE" : "NEGATIVE";

        // Step 2: Collect top common tokens for the chosen sentiment
        List<Map.Entry<String, Integer>> topTokens = getTopCommonTokens(tokenizedReviews, isPositive, 20);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Most common meaningful words from " + sentimentType + " reviews:");
        System.out.println("=".repeat(70));

        // Display tokens with their counts
        for (int i = 0; i < topTokens.size(); i++) {
            Map.Entry<String, Integer> entry = topTokens.get(i);
            String displayWord = getReadableWord(entry.getKey());
            System.out.printf("%2d. %-20s (used %,6d times)\n", i + 1, displayWord, entry.getValue());
        }

        // Step 3: Get user's token choice
        System.out.print("\nEnter the word you want to compare: ");
        String userInput = scanner.nextLine().trim().toLowerCase();

        // Convert user input back to stem if needed
        String chosenToken = getStemFromInput(userInput);

        // Step 4: Analyze token across all airlines
        analyzeTokenAcrossAirlines(airlineReviews, chosenToken, isPositive);
    }

    /**
     * Get top N most common tokens from reviews based on sentiment
     * First gets top N, then filters out meaningless comparison words from that
     * list
     */
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

        // Sort by frequency and get top N
        List<Map.Entry<String, Integer>> sortedTokens = new ArrayList<>(tokenFrequency.entrySet());
        sortedTokens.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Get top N first
        List<Map.Entry<String, Integer>> topNTokens = sortedTokens.subList(0, Math.min(topN, sortedTokens.size()));

        // Then filter out excluded words from the top N
        List<Map.Entry<String, Integer>> filteredTokens = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topNTokens) {
            if (!EXCLUDED_COMPARISON_WORDS.contains(entry.getKey())) {
                filteredTokens.add(entry);
            }
        }

        return filteredTokens;
    }

    /**
     * Collect all unique tokens from reviews based on sentiment
     */
    private static Set<String> collectAllTokens(List<AirlineReview> tokenizedReviews, boolean isPositive) {
        Set<String> tokens = new HashSet<>();

        for (AirlineReview review : tokenizedReviews) {
            boolean reviewIsPositive = "1".equals(review.getRecommended());
            if (reviewIsPositive == isPositive) {
                for (String token : review.getTokenizedReview()) {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }

    /**
     * Analyze and compare all airlines for a specific token
     */
    private static void analyzeTokenAcrossAirlines(HashMap<String, List<AirlineReview>> airlineReviews,
            String token, boolean isPositive) {

        String sentimentType = isPositive ? "POSITIVE" : "NEGATIVE";
        final int MIN_REVIEWS = 50; // Minimum reviews to be considered reliable

        // Data structure to store airline token statistics
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

                // Calculate weighted score: percentage * log(reviews) to balance frequency and
                // sample size
                // This gives higher weight to airlines with more reviews while still
                // considering percentage
                this.weightedScore = totalReviews >= MIN_REVIEWS
                        ? percentage * Math.log10(totalReviews + 1)
                        : percentage * Math.log10(totalReviews + 1) * 0.5; // Penalty for low review count
            }

            @Override
            public int compareTo(AirlineTokenStats other) {
                // Sort by weighted score descending
                return Double.compare(other.weightedScore, this.weightedScore);
            }
        }

        List<AirlineTokenStats> stats = new ArrayList<>();

        // Analyze each airline
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

            // Only include airlines that have the token
            if (tokenCount > 0) {
                stats.add(new AirlineTokenStats(airline, tokenCount, sentimentReviewCount));
            }
        }

        Collections.sort(stats);

        // Display results - Only show top 5
        String displayWord = getReadableWord(token);
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("  COMPARISON RESULTS FOR: \"" + displayWord.toUpperCase() + "\"");
        System.out.println("  Sentiment: " + sentimentType + " reviews");
        System.out.println("  Total airlines with this word: " + stats.size());
        System.out.println("  Minimum reviews for reliability: " + MIN_REVIEWS);
        System.out.println("=".repeat(70));

        // Show top 5
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

        // Footer explanation
        System.out.println("\n" + "=".repeat(70));
        System.out.println("* Airlines with < " + MIN_REVIEWS + " reviews may have less reliable percentages");
        System.out.println("Weighted Score Formula: percentage × log₁₀(reviews + 1)");
        System.out.println("This balances token frequency with statistical reliability");
        System.out.println("=".repeat(70));
    }
}