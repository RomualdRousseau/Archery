package com.github.romualdrousseau.archery.commons.strings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class StringFuzzy {
    public static float Hamming(String s, String t) {
        if(s.length() != t.length()) {
            return 0.0f;
        }

        if (s.length() == 0) {
            return 0.0f;
        }

        int n = 0;
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == t.charAt(i)) {
                n++;
            }
        }
        return (float) Math.exp(n) / (float) Math.exp(s.length());
    }

    public static float JaroWinkler(String s, String t) {
        int s_len = s.length();
        int t_len = t.length();

        if (s_len == 0 && t_len == 0) {
            return 1.0f;
        }

        int match_distance = Integer.max(s_len, t_len) / 2 - 1;

        boolean[] s_matches = new boolean[s_len];
        boolean[] t_matches = new boolean[t_len];

        int matches = 0;
        int transpositions = 0;

        for (int i = 0; i < s_len; i++) {
            int start = Integer.max(0, i - match_distance);
            int end = Integer.min(i + match_distance + 1, t_len);

            for (int j = start; j < end; j++) {
                if (t_matches[j])
                    continue;
                if (s.charAt(i) != t.charAt(j))
                    continue;
                s_matches[i] = true;
                t_matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0)
            return 0;

        int k = 0;
        for (int i = 0; i < s_len; i++) {
            if (!s_matches[i])
                continue;
            while (!t_matches[k])
                k++;
            if (s.charAt(i) != t.charAt(k))
                transpositions++;
            k++;
        }

        return ((((float) matches / (float) s_len) + ((float) matches / (float) t_len)
                + (((float) matches - (float) transpositions / 2.0f) / (float) matches)) / 3.0f);
    }

    public static float Jaccard(String s1, String s2) {
        return Float.valueOf(StringFuzzy.intersect(s1, s2).length())
                / Float.valueOf(StringFuzzy.union(s1, s2).length());
    }

    public static String union(String s1, String s2) {
        String result = "";

        for (char c : s1.toCharArray()) {
            if (!result.contains(String.valueOf(c))) {
                result += c;
            }
        }

        for (char c : s2.toCharArray()) {
            if (!result.contains(String.valueOf(c))) {
                result += c;
            }
        }

        return result;
    }

    public static String[] union(String[] s1, String[] s2) {
        ArrayList<String> result = new ArrayList<String>(s1.length + s2.length);

        for (String v : s1) {
            if (!result.contains(v)) {
                result.add(v);
            }
        }

        for (String v : s2) {
            if (!result.contains(v)) {
                result.add(v);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    public static String intersect(String s1, String s2) {
        String result = "";

        for (char c : s1.toCharArray()) {
            if (!result.contains(String.valueOf(c)) && s2.contains(String.valueOf(c))) {
                result += c;
            }
        }

        return result;
    }

    public static String[] intersect(String[] s1, String[] s2) {
        ArrayList<String> result = new ArrayList<String>(s1.length + s2.length);
        List<String> tmp = Arrays.asList(s2);

        for (String v : s1) {
            if (!result.contains(v) && tmp.contains(v)) {
                result.add(v);
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
