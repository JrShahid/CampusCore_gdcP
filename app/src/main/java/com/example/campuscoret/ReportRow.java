package com.example.campuscoret;

public class ReportRow {
    private final String title;
    private final String lineOne;
    private final String lineTwo;
    private final String lineThree;

    public ReportRow(String title, String lineOne, String lineTwo, String lineThree) {
        this.title = title;
        this.lineOne = lineOne;
        this.lineTwo = lineTwo;
        this.lineThree = lineThree;
    }

    public String getTitle() {
        return title;
    }

    public String getLineOne() {
        return lineOne;
    }

    public String getLineTwo() {
        return lineTwo;
    }

    public String getLineThree() {
        return lineThree;
    }
}
