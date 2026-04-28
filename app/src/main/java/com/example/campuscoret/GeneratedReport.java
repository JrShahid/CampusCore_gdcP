package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public class GeneratedReport {
    private final String title;
    private final String summary;
    private final String exportText;
    private final List<ReportRow> rows;

    public GeneratedReport(String title, String summary, String exportText, List<ReportRow> rows) {
        this.title = title;
        this.summary = summary;
        this.exportText = exportText;
        this.rows = new ArrayList<>(rows);
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getExportText() {
        return exportText;
    }

    public List<ReportRow> getRows() {
        return new ArrayList<>(rows);
    }
}
