package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.TextAnalysisUtils;
import org.example.model.util.WordCount;

public class AirlineBSTImpl {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineBSTImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        // Build BSTs for good and bad words
        WordFrequencyBST goodBST = new WordFrequencyBST();
        WordFrequencyBST badBST = new WordFrequencyBST();

        int goodTotal = 0;
        int badTotal = 0;

        for (AirlineReview review : reviews) {
            if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                for (String word : review.getTokenizedReview()) {
                    goodBST.insert(word);
                    goodTotal++;
                }
            } else {
                for (String word : review.getTokenizedReview()) {
                    badBST.insert(word);
                    badTotal++;
                }
            }
        }

        // Convert BST to sorted lists
        List<WordCount> goodCounts = goodBST.toWordCountList(goodTotal);
        List<WordCount> badCounts = badBST.toWordCountList(badTotal);

        // Filter near-common dominant words
        ArrayList<WordCount> filteredGood = new ArrayList<>();
        ArrayList<WordCount> filteredBad = new ArrayList<>();
        TextAnalysisUtils.filterNearCommonDominant(goodCounts, badCounts, 0.30, filteredGood, filteredBad);

        // Get top 10
        List<WordCount> topGood = TextAnalysisUtils.takeTopK(filteredGood, 10);
        List<WordCount> topBad = TextAnalysisUtils.takeTopK(filteredBad, 10);

        return Pair.of(topGood, topBad);
    }

    // Inner class: BST Node
    private static class BSTNode {
        String word;
        int count;
        BSTNode left;
        BSTNode right;

        BSTNode(String word) {
            this.word = word;
            this.count = 1;
            this.left = null;
            this.right = null;
        }
    }

    // Inner class: Word Frequency BST
    private static class WordFrequencyBST {
        private BSTNode root;

        public WordFrequencyBST() {
            this.root = null;
        }

        // Insert word or increment count if exists
        public void insert(String word) {
            root = insertRec(root, word);
        }

        private BSTNode insertRec(BSTNode node, String word) {
            if (node == null) {
                return new BSTNode(word);
            }

            int cmp = word.compareTo(node.word);
            if (cmp < 0) {
                node.left = insertRec(node.left, word);
            } else if (cmp > 0) {
                node.right = insertRec(node.right, word);
            } else {
                // Word already exists, increment count
                node.count++;
            }

            return node;
        }

        // In-order traversal to get sorted list of WordCount objects
        public List<WordCount> toWordCountList(int totalWords) {
            List<WordCount> result = new ArrayList<>();
            inOrderTraversal(root, result, totalWords);
            return result;
        }

        private void inOrderTraversal(BSTNode node, List<WordCount> result, int totalWords) {
            if (node == null) {
                return;
            }

            inOrderTraversal(node.left, result, totalWords);
            result.add(new WordCount(node.word, node.count, totalWords));
            inOrderTraversal(node.right, result, totalWords);
        }
    }
}
