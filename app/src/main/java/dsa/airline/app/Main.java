package dsa.airline.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.management.ManagementFactory;
import com.sun.management.ThreadMXBean;

import dsa.airline.ds.AirlineArrayListImpl;
import dsa.airline.ds.AirlineBSTImpl;
import dsa.airline.ds.AirlineMapImpl;
import dsa.airline.ds.AirlineRBTreeImpl;
import dsa.airline.ds.WordFrequencyAnalyzer;
import dsa.airline.model.AirlineReview;
import dsa.airline.model.util.Pair;
import dsa.airline.model.util.WordCount;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.en.PorterStemFilter;

/*
 * Memory-safe streaming version
 * CSV columns:
 * 0: Airline, 6: Review text, 11: Overall rating, 19: Recommended
 */
public class Main {
    public static Set<String> uselessWord = Set.of(
            "i", "you", "we", "my", "were", "have", "had", "us", "our", "so", "from");

    /** Parse a single logical CSV line with quotes/commas */
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
                field.setLength(0);
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

        try {
            analyzer.getTop10MostCommonWords();
        } catch (Exception ignore) {
        }

        final int runs = 5;
        long[] timesMs = new long[runs];
        long[] allocatedBytes = new long[runs];
        ThreadMXBean tmb = null;
        try {
            java.lang.management.ThreadMXBean base = ManagementFactory.getThreadMXBean();
            if (base instanceof ThreadMXBean) {
                tmb = (ThreadMXBean) base;
                if (tmb.isThreadAllocatedMemorySupported() && !tmb.isThreadAllocatedMemoryEnabled()) {
                    tmb.setThreadAllocatedMemoryEnabled(true);
                }
            }
        } catch (Throwable ignore) {
        }

        Pair<List<WordCount>, List<WordCount>> top10 = null;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            long beforeAlloc = -1L;
            long tid = Thread.currentThread().getId();
            if (tmb != null && tmb.isThreadAllocatedMemorySupported() && tmb.isThreadAllocatedMemoryEnabled()) {
                beforeAlloc = tmb.getThreadAllocatedBytes(tid);
            }
            long startNs = System.nanoTime();
            top10 = analyzer.getTop10MostCommonWords();
            long endNs = System.nanoTime();

            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            timesMs[r] = (endNs - startNs) / 1_000_000;
            if (beforeAlloc >= 0) {
                long afterAlloc = tmb.getThreadAllocatedBytes(tid);
                allocatedBytes[r] = Math.max(0, afterAlloc - beforeAlloc);
            } else {
                allocatedBytes[r] = -1L;
            }
        }

        Arrays.sort(timesMs);
        Arrays.sort(allocatedBytes);
        long medianTime = timesMs[runs / 2];
        long medianAlloc = allocatedBytes[runs / 2];
        if (medianAlloc >= 0) {
            System.out.printf("Time (median of %d): %d ms | Allocated (median): %d KB%n",
                    runs, medianTime, medianAlloc / 1024);
        } else {
            System.out.printf("Time (median of %d): %d ms | Allocated (median): unsupported%n",
                    runs, medianTime);
        }

        System.out.println("\n[GOOD] Top 10 most common words in POSITIVE reviews:");
        System.out.println("-----------------------------------------");
        int goodRank = 1;
        for (WordCount wc : top10.getLeft()) {
            System.out.printf("%2d. Word: %-15s Count: %5d%n", goodRank++, wc.getWord(), wc.getCount());
        }

        System.out.println("\n[BAD] Top 10 most common words in NEGATIVE reviews:");
        System.out.println("-----------------------------------------");
        int badRank = 1;
        for (WordCount wc : top10.getRight()) {
            System.out.printf("%2d. Word: %-15s Count: %5d%n", badRank++, wc.getWord(), wc.getCount());
        }
    }

    public static void main(String[] args) throws IOException {
        // Limit memory by focusing on one airline first (change/remove later)
        final String testAirline = "spirit-airlines";

        // Structure consumed by your analyzers
        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();

        // Optional: dev cap (use -Drow.limit=200000)
        final int limit = Integer.getInteger("row.limit", 0);
        int seen = 0;

        // Load from resources
        try (InputStream csvStream = Main.class.getClassLoader().getResourceAsStream("airline.csv")) {
            if (csvStream == null) {
                System.out.println("ERROR: Could not find airline.csv in resources folder!");
                return;
            }
            System.out.println("✓ Found airline.csv in resources");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));
                    Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet())) {

                // Skip header
                String line = br.readLine();

                StringBuilder currentLine = new StringBuilder(4096);
                boolean inQuotes = false;

                while ((line = br.readLine()) != null) {
                    currentLine.append(line);

                    // track quotes for multi-line fields (simple toggle; doubled quotes handled in
                    // parse)
                    for (int i = 0; i < line.length(); i++) {
                        if (line.charAt(i) == '"')
                            inQuotes = !inQuotes;
                    }
                    if (inQuotes) {
                        currentLine.append('\n');
                        continue;
                    }

                    // complete logical row
                    String[] row = parseCSVLine(currentLine.toString());
                    currentLine.setLength(0); // reuse buffer

                    String airline = row.length > 0 && !row[0].isEmpty() ? row[0] : null;
                    String reviewText = row.length > 6 && !row[6].isEmpty() ? row[6] : null;
                    String overall = row.length > 11 && !row[11].isEmpty() ? row[11] : null;
                    String recommended = row.length > 19 && !row[19].isEmpty() ? row[19] : null;

                    if (airline == null || reviewText == null)
                        continue;
                    // Removed airline filter to load ALL airlines for interactive comparison
                    // if (!testAirline.equals(airline))
                    // continue; // memory saver for now
                    if (limit > 0 && ++seen > limit)
                        break;

                    // tokenize this review only; don't accumulate globally
                    List<String> tokens = new ArrayList<>(64);
                    TokenStream ts = analyzer.tokenStream(null, new StringReader(reviewText));
                    TokenStream stemmed = new PorterStemFilter(ts);
                    try {
                        CharTermAttribute attr = stemmed.addAttribute(CharTermAttribute.class);
                        stemmed.reset();
                        while (stemmed.incrementToken()) {
                            String w = attr.toString();
                            if (!uselessWord.contains(w))
                                tokens.add(w);
                        }
                        stemmed.end();
                    } finally {
                        stemmed.close(); // closes ts too
                    }

                    if (!tokens.isEmpty()) {
                        airlineReviews
                                .computeIfAbsent(airline, k -> new ArrayList<>())
                                .add(new AirlineReview(airline, tokens.toArray(new String[0]), overall, recommended));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading/processing CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // Run analyzers on spirit-airlines only (for performance demonstration)
        // Note: airlineReviews contains ALL airlines, but we analyze just one for the
        // top-10 demo
        runAnalyzer("BST", new AirlineBSTImpl(airlineReviews, testAirline));
        runAnalyzer("ArrayList", new AirlineArrayListImpl(airlineReviews, testAirline));
        runAnalyzer("Red-Black Tree", new AirlineRBTreeImpl(airlineReviews, testAirline));
        runAnalyzer("Map", new AirlineMapImpl(airlineReviews, testAirline));

        // Create tokenized reviews list for interactive comparison (uses ALL airlines)
        List<AirlineReview> tokenizedReviews = new ArrayList<>();
        for (List<AirlineReview> reviews : airlineReviews.values()) {
            tokenizedReviews.addAll(reviews);
        }

        // Run interactive comparison (compares across ALL airlines)
        InteractiveComparison.run(airlineReviews, tokenizedReviews);
    }
}