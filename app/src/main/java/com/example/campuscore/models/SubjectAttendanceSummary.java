package com.example.campuscore.models;

public class SubjectAttendanceSummary {
    private final String subject;
    private final int presentCount;
    private final int totalCount;
    private final double percentage;

    public SubjectAttendanceSummary(String subject, int presentCount, int totalCount, double percentage) {
        this.subject = subject;
        this.presentCount = presentCount;
        this.totalCount = totalCount;
        this.percentage = percentage;
    }

    public String getSubject() {
        return subject;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public double getPercentage() {
        return percentage;
    }
}
