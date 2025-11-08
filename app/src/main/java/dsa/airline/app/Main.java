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

        // Warm-up (JIT, caches)
        try {
            analyzer.getTop10MostCommonWords();
        } catch (Exception ignore) {}

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
        } catch (Throwable ignore) {}

        Pair<List<WordCount>, List<WordCount>> top10 = null;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            long beforeAlloc = -1L;
            long tid = Thread.currentThread().getId();
            if (tmb != null && tmb.isThreadAllocatedMemorySupported() && tmb.isThreadAllocatedMemoryEnabled()) {
                beforeAlloc = tmb.getThreadAllocatedBytes(tid);
            }
            long startNs = System.nanoTime();

            top10 = analyzer.getTop10MostCommonWords();

            long endNs = System.nanoTime();
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            timesMs[r] = (endNs - startNs) / 1_000_000;
            if (beforeAlloc >= 0) {
                long afterAlloc = tmb.getThreadAllocatedBytes(tid);
                allocatedBytes[r] = Math.max(0, afterAlloc - beforeAlloc);
            } else {
                allocatedBytes[r] = -1L; // unsupported
            }
        }

        Arrays.sort(timesMs);
        Arrays.sort(allocatedBytes);
        long medianTime = timesMs[runs / 2];
        long medianAlloc = allocatedBytes[runs / 2];
        if (medianAlloc >= 0) {
            long medianAllocKB = medianAlloc / 1024;
            System.out.printf("Time (median of %d): %d ms | Allocated (median): %d KB%n", runs, medianTime, medianAllocKB);
        } else {
            System.out.printf("Time (median of %d): %d ms | Allocated (median): unsupported on this JVM%n", runs, medianTime);
        }

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
        InteractiveComparison.run(airlineReviews, tokenizedReviews);
    }
}