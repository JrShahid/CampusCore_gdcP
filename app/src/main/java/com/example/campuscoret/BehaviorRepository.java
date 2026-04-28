package com.example.campuscoret;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BehaviorRepository {
    private static final Map<String, BehaviorRating> RATINGS = buildSeedRatings();

    private BehaviorRepository() {
    }

    public static void upsertRating(
            String studentId,
            String studentName,
            int discipline,
            int participation,
            int punctuality,
            int respectfulness,
            String teacherEmail
    ) {
        BehaviorRating rating = new BehaviorRating(
                studentId,
                studentName,
                discipline,
                participation,
                punctuality,
                respectfulness,
                teacherEmail,
                System.currentTimeMillis()
        );
        RATINGS.put(studentId, rating);
        FirebaseCampusSync.publishBehaviorRating(rating);
    }

    public static BehaviorRating getRatingForStudent(String studentId) {
        return RATINGS.get(studentId);
    }

    public static int getBehaviorScore(String studentId) {
        BehaviorRating rating = RATINGS.get(studentId);
        return rating == null ? 0 : rating.getAverageScore();
    }

    private static Map<String, BehaviorRating> buildSeedRatings() {
        Map<String, BehaviorRating> ratings = new LinkedHashMap<>();
        ratings.put("aarav@campus.edu", new BehaviorRating("aarav@campus.edu", "Aarav Sharma", 9, 8, 9, 9, "teacher@campus.edu", System.currentTimeMillis()));
        ratings.put("diya@campus.edu", new BehaviorRating("diya@campus.edu", "Diya Patel", 8, 9, 8, 9, "teacher@campus.edu", System.currentTimeMillis()));
        ratings.put("rohan@campus.edu", new BehaviorRating("rohan@campus.edu", "Rohan Verma", 7, 6, 7, 8, "teacher@campus.edu", System.currentTimeMillis()));
        ratings.put("meera@campus.edu", new BehaviorRating("meera@campus.edu", "Meera Nair", 9, 9, 8, 9, "teacher@campus.edu", System.currentTimeMillis()));
        ratings.put("kabir@campus.edu", new BehaviorRating("kabir@campus.edu", "Kabir Singh", 8, 7, 8, 8, "teacher@campus.edu", System.currentTimeMillis()));
        ratings.put("sana@campus.edu", new BehaviorRating("sana@campus.edu", "Sana Khan", 6, 7, 6, 7, "teacher@campus.edu", System.currentTimeMillis()));
        return ratings;
    }

    static void replaceRatingsFromFirebase(Map<String, BehaviorRating> ratings) {
        RATINGS.clear();
        if (ratings != null) {
            RATINGS.putAll(ratings);
        }
    }

    static Map<String, BehaviorRating> exportRatings() {
        return new LinkedHashMap<>(RATINGS);
    }
}
