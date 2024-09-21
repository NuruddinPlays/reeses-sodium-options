package me.flashyreese.mods.reeses_sodium_options.util;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;

public class StringUtils {

    // Normalize text for localization handling
    public static String normalizeText(String text) {
        text = text.toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        return text;
    }

    // Levenshtein distance algorithm for approximate matching
    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    // Combined search function
    public static <T> List<T> searchElements(Iterable<T> elements, String query, Function<T, String> extractSearchableText) {
        String normalizedQuery = normalizeText(query);
        List<T> exactMatches = new ArrayList<>();
        List<T> approxMatches = new ArrayList<>();

        for (T element : elements) {
            String normalizedName = normalizeText(extractSearchableText.apply(element));

            if (boyerMooreSearch(normalizedName, normalizedQuery) != -1) {
                exactMatches.add(element);
            } else {
                int nameDistance = levenshteinDistance(normalizedName, normalizedQuery);
                if (nameDistance < normalizedQuery.length() / 2) {
                    approxMatches.add(element);
                }
            }
        }

        // Sort approximate matches by Levenshtein distance
        // For future use
        /*approxMatches.sort(Comparator.comparingInt(entry ->
                levenshteinDistance(normalizeText(extractSearchableText.apply(entry)), normalizedQuery)
        ));*/

        List<T> results = new ArrayList<>(exactMatches);
        results.addAll(approxMatches);
        return results;
    }

    // Boyer-Moore with good suffix and bad character heuristics
    public static int boyerMooreSearch(String text, String pattern) {
        pattern = normalizeText(pattern); // Ensure pattern is normalized too
        int patternLength = pattern.length();
        int textLength = text.length();

        if (patternLength == 0) return 0; // Empty pattern matches at the start of the text

        Map<Character, Integer> badCharTable = new HashMap<>();
        buildBadCharTable(badCharTable, pattern);

        int[] suffixArray = new int[patternLength + 1];
        int[] shiftTable = new int[patternLength + 1];
        Arrays.fill(shiftTable, 0);

        computeFullShiftTable(shiftTable, suffixArray, pattern);
        computeGoodSuffixShiftTable(shiftTable, suffixArray, pattern);

        int shift = 0;
        while (shift <= (textLength - patternLength)) {
            int j = patternLength - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) {
                return shift; // Pattern found, return the index
            } else {
                char mismatchedChar = text.charAt(shift + j);
                int badCharShift = badCharTable.getOrDefault(mismatchedChar, -1);
                int goodSuffixShift = j + 1 < patternLength ? shiftTable[j + 1] : 1; // Boundary check

                shift += Math.max(j - badCharShift, goodSuffixShift);
            }
        }
        return -1; // Pattern not found
    }

    // Method to build the full suffix shift table
    public static void computeFullShiftTable(int[] shiftTable, int[] suffixArray, String pattern) {
        int patternLength = pattern.length();
        int i = patternLength;
        int j = patternLength + 1;
        suffixArray[i] = j;

        while (i > 0) {
            while (j <= patternLength && pattern.charAt(i - 1) != pattern.charAt(j - 1)) {
                if (shiftTable[j] == 0) {
                    shiftTable[j] = j - i;
                }
                j = suffixArray[j];
            }
            i--;
            j--;
            suffixArray[i] = j;
        }
    }

    // Method to compute the good suffix shift table
    public static void computeGoodSuffixShiftTable(int[] shiftTable, int[] suffixArray, String pattern) {
        int patternLength = pattern.length();
        int j = suffixArray[0];

        for (int i = 0; i < patternLength; i++) {
            if (shiftTable[i] == 0) {
                shiftTable[i] = j;
            }
            if (i == j) {
                j = suffixArray[j];
            }
        }
    }

    // Method to build the bad character heuristic table
    public static void buildBadCharTable(Map<Character, Integer> badCharTable, String pattern) {
        int patternLength = pattern.length();
        for (int i = 0; i < patternLength - 1; i++) {
            badCharTable.put(pattern.charAt(i), i);
        }
    }
}