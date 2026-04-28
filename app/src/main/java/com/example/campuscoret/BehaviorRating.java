package com.example.campuscoret;

public class BehaviorRating {
    private final String studentId;
    private final String studentName;
    private final int discipline;
    private final int participation;
    private final int punctuality;
    private final int respectfulness;
    private final String teacherEmail;
    private final long updatedAtMillis;

    public BehaviorRating(
            String studentId,
            String studentName,
            int discipline,
            int participation,
            int punctuality,
            int respectfulness,
            String teacherEmail,
            long updatedAtMillis
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.discipline = discipline;
        this.participation = participation;
        this.punctuality = punctuality;
        this.respectfulness = respectfulness;
        this.teacherEmail = teacherEmail;
        this.updatedAtMillis = updatedAtMillis;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getDiscipline() {
        return discipline;
    }

    public int getParticipation() {
        return participation;
    }

    public int getPunctuality() {
        return punctuality;
    }

    public int getRespectfulness() {
        return respectfulness;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public int getAverageScore() {
        return Math.round((discipline + participation + punctuality + respectfulness) / 4f * 10f);
    }
}
