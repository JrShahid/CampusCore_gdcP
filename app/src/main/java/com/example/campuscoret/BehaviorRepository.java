package com.example.campuscoret;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BehaviorRepository {
    private static final Map<String, BehaviorRating> RATINGS = new LinkedHashMap<>();

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

    static void replaceRatingsFromFirebase(Map<String, BehaviorRating> ratings) {
        RATINGS.clear();
        if (ratings != null) {
            RATINGS.putAll(ratings);
        }
    }

}
