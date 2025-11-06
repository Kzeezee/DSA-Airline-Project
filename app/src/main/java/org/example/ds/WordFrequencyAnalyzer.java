package org.example.ds;

import java.util.List;

import org.example.model.util.Pair;
import org.example.model.util.WordCount;

public interface WordFrequencyAnalyzer {
    Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords();
}


