package org.example.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.en.PorterStemFilter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.example.ds.arraylist.AirlineArrayListImpl;
import org.example.model.AirlineReview;
import org.example.model.WordCount;
import org.example.util.Pair;

public class Main {
    public static void arrayListImplementationTest(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        AirlineArrayListImpl airlineArrayListImpl = new AirlineArrayListImpl(airlineReviews, airline);
        Pair<ArrayList<WordCount>, ArrayList<WordCount>> top10MostCommonWords = airlineArrayListImpl.getTop10MostCommonWords();
        System.out.println("Top 10 most common words in good reviews: ");
        for (WordCount wc : top10MostCommonWords.getLeft()) {
            System.out.println(wc);
        }
        System.out.println("Top 10 most common words in bad reviews: ");
        for (WordCount wc : top10MostCommonWords.getRight()) {
            System.out.println(wc);
        }
    }

    public static void main(String[] args) throws IOException {
        List<String[]> records = new ArrayList<>();

        InputStream input = Main.class.getClassLoader().getResourceAsStream("airline.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(input))) {
            List<String[]> allRows = reader.readAll();
            for (String[] row : allRows) {
                String[] selectedValues = new String[4];

                selectedValues[0] = row.length > 0 ? (row[0].isEmpty() ? null : row[0]) : null; // airline
                selectedValues[1] = row.length > 6 ? (row[6].isEmpty() ? null : row[6]) : null; // review content
                selectedValues[2] = row.length > 11 ? (row[11].isEmpty() ? null : row[11]) : null; // overall rating
                selectedValues[3] = row.length > 19 ? (row[19].isEmpty() ? null : row[19]) : null; // recommended

                for (String value : selectedValues) {
                    if (value == null) {
                        continue;
                    }
                }
                records.add(selectedValues);
            }

        } catch (IOException | CsvException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

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

                        while (stemmedStream.incrementToken()) {
                            tokens.add(attr.toString());
                        }
                        stemmedStream.end();
                    }

                    if (!tokens.isEmpty()) {
                        tokenizedReviews.add(new AirlineReview(records.get(i)[0],
                                tokens.toArray(new String[tokens.size()]), records.get(i)[2], records.get(i)[3]));
                    }
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error processing reviews: " + e.getMessage());
            e.printStackTrace();
        }

        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();
        for (AirlineReview airlineReview : tokenizedReviews) {
            if (!airlineReviews.containsKey(airlineReview.getAirline())) {
                airlineReviews.put(airlineReview.getAirline(), new ArrayList<>());
            }
            airlineReviews.get(airlineReview.getAirline()).add(airlineReview);
        }

        arrayListImplementationTest(airlineReviews, "spirit-airlines");
    }
}

