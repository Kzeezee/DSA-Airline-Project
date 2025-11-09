# ✈️ CS201: Reimagine Sentiment Analysis for Airline Reviews Insights
![license](https://img.shields.io/badge/license-MIT-2ea44f)
![java](https://img.shields.io/badge/java-17%2B-007396?logo=openjdk&logoColor=white)
![python](https://img.shields.io/badge/python-3.x-3776AB?logo=python&logoColor=white)
![last commit](https://img.shields.io/badge/last%20commit-november%202025-gold)

For detailed methodology, findings, and comprehensive analysis, please see the full report:

📥 [See Complete Report (PDF)](./Presentation%20Slide.pdf)

The **Airline Sentiment Analysis** project offers a comprehensive implementation of airline-review text analysis, implementing multiple data-structure to compute the top-K most common tokens across positive and negative reviews.

---

## Table of Contents
- [What this project does](#what-this-project-does)
- [Project structure (high level)](#project-structure-high-level)
- [How to run](#how-to-run)
- [Example output (truncated)](#example-output-truncated)
- [Interactive Comparison (detailed)](#interactive-comparison-detailed)

---

## What this project does

  - Binary Search Tree (BST) implementation (`AirlineBSTImpl`) - keeps a BST of unique words and counts occurrences on insert.
  - Red-Black Tree (RBT) implementation (`AirlineRBTreeImpl`) - a self-balancing BST with color-based rotations to guarantee O(log N) operations.
  - ArrayList-based implementation (`AirlineArrayListImpl`) - accumulates tokens then compresses sorted runs to counts.
  - Map implementation (`AirlineMapImpl`) - count frequency of word with hash map and acquire top 10.

---

## Project structure (high level)

  - `AirlineBSTImpl.java` - Binary Search Tree version
  - `AirlineRBTreeImpl.java` - Red-Black Tree (self-balancing) version
  - `AirlineArrayListImpl.java` - ArrayList-based version
  - `AirlineMapImpl.java` - Map-based version
  - `WordFrequencyAnalyzer.java` - simple interface implemented by each approach
  - `AirlineReview.java` - review container (airline, tokenized review, rating, recommended)
  - `WordCount.java` - wrapper: word + count + total for percent computations
  - `Pair.java` - simple left/right pair returned by analyzers
  - `TextAnalysisUtils.java` - helpers for compressing counts, filtering near-common dominant words, and top-K helpers

## How to run

Open a terminal in the project root (Windows cmd / PowerShell) and run the Gradle wrapper. Use the commands below depending on your shell:

Windows (cmd.exe):

```bat
gradlew.bat run
```

PowerShell (or Git Bash / WSL):

```powershell
.\gradlew run
```

What the run does:
  - Loads and tokenizes the dataset from `app/src/main/resources/airline.csv` (stemming + stop word removal).
  - Runs all four analyzers (BST, Red-Black Tree, ArrayList, Map) on the same airline and prints:
    - Time (median of 5) and Allocated (median KB) for `getTop10MostCommonWords()`.
    - Top 10 tokens for positive (GOOD) and negative (BAD) reviews in a consistent format.
  - Prompts to launch the interactive token comparison CLI.

---

## Example output (truncated)

```
[BST] Time (median of 5): 23 ms | Allocated (median): 330 KB
[GOOD] Top 10 most common words in POSITIVE reviews:
 1. Word: about          Count: 136
 ...
[BAD] Top 10 most common words in NEGATIVE reviews:
 1. Word: hour           Count: 679
 ...
```

---

## Interactive Comparison (detailed)

The project includes an interactive CLI to compare how a specific token (word stem) is used across airlines. This CLI is implemented in `InteractiveComparison` and is invoked at the end of the static runs from `Main.java`.

Flow and prompts:

- Prompt 1: "Would you like to compare airlines based on a specific word? (yes/no)" - answer `yes` to continue, `no` to skip and exit.
- Prompt 2: "Compare based on: 1. Positive reviews (good words) 2. Negative reviews (bad words)" - enter `1` or `2`.
- The program computes the top ~20 most common tokens for the chosen sentiment across the entire dataset, excluding a small set of uninformative stems.
- It prints the tokens (mapped to more readable words when available) and asks you to type the word you want to compare.

What the comparison computes:

- For every airline, it counts how many times the chosen token appears in reviews of the selected sentiment and how many reviews of that sentiment the airline has.
- Percentage = tokenCount / sentimentReviewCount (per-airline).
- Weighted score = percentage * log10(totalReviews + 1). Airlines with fewer than `MIN_REVIEWS` (50) receive a 0.5 penalty multiplier to de-prioritize low-data results.
- Airlines are ranked by the weighted score and the top 5 are shown with columns: Reviews, Count, Percent, Score. Low-data airlines are marked with `*`.

Important constants and behaviors:

- `EXCLUDED_COMPARISON_WORDS` - a small, hard-coded set of stems to ignore when presenting token choices.
- `STEM_TO_WORD` - a fixed mapping from stems to readable words (used for presentation and to accept readable input from the user).
- `MIN_REVIEWS = 50` - reliability threshold; below this, results are penalized.
- CLI uses `Scanner` for input; running in non-interactive environments will block the process - answer `no` to skip the interactive step or comment out the call in `Main.java`.

Example (mock) interaction:

```
Would you like to compare airlines based on a specific word? (yes/no): yes
Compare based on:
1. Positive reviews (good words)
2. Negative reviews (bad words)
Enter choice (1 or 2): 2

Most common meaningful words from NEGATIVE reviews:
 1. delay          (used 2,345 times)
 2. hour           (used 1,102 times)
 3. seat           (used 987 times)
 ...
Enter the word you want to compare: delay

TOP 5 Airlines with HIGHEST weighted score for "delay":
Rank  Airline                     Reviews   Count   Percent   Score
1     Airline A                    1,200     240     20.00%   26.45
2     Airline B                    3,400     540     15.88%   24.30
3     Airline C                      40      18     45.00%    4.32 *
```
