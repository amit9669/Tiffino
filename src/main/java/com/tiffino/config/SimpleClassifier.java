package com.tiffino.config;

import java.util.*;
import java.util.regex.Pattern;

public class SimpleClassifier {

    public enum Type { MISMATCH, SPOILED, INSECT, BAD_TASTE, UNKNOWN }

    public static class Classification {
        public final Type type;
        public final double confidence;
        public final List<String> matched;

        public Classification(Type type, double confidence, List<String> matched) {
            this.type = type;
            this.confidence = confidence;
            this.matched = matched;
        }

        @Override
        public String toString() {
            return "Classification{" +
                    "type=" + type +
                    ", confidence=" + confidence +
                    ", matched=" + matched +
                    '}';
        }
    }

    private final Map<Type, List<Pattern>> patterns = new EnumMap<>(Type.class);

    public SimpleClassifier() {
        patterns.put(Type.MISMATCH, Arrays.asList(
                Pattern.compile("\\b(wrong item|different dish|not what I ordered|mismatch|incorrect)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(received.*wrong|delivered.*wrong)\\b", Pattern.CASE_INSENSITIVE)
        ));

        patterns.put(Type.SPOILED, Arrays.asList(
                Pattern.compile("\\b(spoiled|rotten|expired|sour smell|bad smell)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(gone bad|smells bad|smelling)\\b", Pattern.CASE_INSENSITIVE)
        ));

        patterns.put(Type.INSECT, Arrays.asList(
                Pattern.compile("\\b(insect|cockroach|worm|fly|bug|ant)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(found.*(insect|bug|cockroach))\\b", Pattern.CASE_INSENSITIVE)
        ));

    }

    public Classification classify(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new Classification(Type.UNKNOWN, 0.0, Collections.emptyList());
        }

        Map<Type, Integer> hits = new EnumMap<>(Type.class);
        Map<Type, List<String>> matched = new EnumMap<>(Type.class);
        for (Type t : Type.values()) {
            hits.put(t, 0);
            matched.put(t, new ArrayList<>());
        }

        for (Map.Entry<Type, List<Pattern>> e : patterns.entrySet()) {
            for (Pattern p : e.getValue()) {
                java.util.regex.Matcher m = p.matcher(text);
                if (m.find()) {
                    hits.put(e.getKey(), hits.get(e.getKey()) + 1);
                    matched.get(e.getKey()).add(m.group());
                }
            }
        }

        Type best = Type.UNKNOWN;
        int bestHits = 0;
        for (Type t : hits.keySet()) {
            if (t == Type.UNKNOWN) continue;
            int h = hits.get(t);
            if (h > bestHits) {
                bestHits = h;
                best = t;
            }
        }

        double confidence = Math.min(1.0, bestHits / 2.0);
        if (best == Type.UNKNOWN && text.length() > 30) {
            return new Classification(Type.UNKNOWN, 0.2, Collections.emptyList());
        }

        return new Classification(best, confidence, matched.get(best));
    }
}
