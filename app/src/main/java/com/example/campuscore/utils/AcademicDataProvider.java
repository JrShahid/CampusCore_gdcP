package com.example.campuscore.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AcademicDataProvider {
    private static final String DEFAULT_DEPARTMENT = "Computer Science";

    private AcademicDataProvider() {
    }

    public static List<String> departmentNames() {
        return new ArrayList<>(departmentCodeMap().keySet());
    }

    public static List<String> semesterLabels() {
        List<String> semesters = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            semesters.add("Semester " + i);
        }
        return semesters;
    }

    public static List<String> semesterValues() {
        List<String> semesters = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            semesters.add(String.valueOf(i));
        }
        return semesters;
    }

    public static List<SubjectItem> subjectsForDepartment(String departmentName) {
        Map<String, List<SubjectItem>> subjectMap = subjectMap();
        if (subjectMap.containsKey(departmentName)) {
            return new ArrayList<>(subjectMap.get(departmentName));
        }
        return new ArrayList<>(subjectMap.get(DEFAULT_DEPARTMENT));
    }

    public static String departmentCode(String departmentName) {
        Map<String, String> codes = departmentCodeMap();
        return codes.containsKey(departmentName) ? codes.get(departmentName) : "GEN";
    }

    public static String semesterValue(String semesterLabel) {
        return semesterLabel.replace("Semester ", "").trim();
    }

    public static String semesterStorageLabel(String semesterLabel) {
        return semesterLabel.replace(" ", "_").trim();
    }

    private static Map<String, String> departmentCodeMap() {
        Map<String, String> codes = new LinkedHashMap<>();
        codes.put("Computer Science", "CS");
        codes.put("Information Technology", "IT");
        codes.put("Electronics", "ECE");
        codes.put("Mechanical", "ME");
        codes.put("Civil", "CE");
        return codes;
    }

    private static Map<String, List<SubjectItem>> subjectMap() {
        Map<String, List<SubjectItem>> map = new LinkedHashMap<>();

        List<SubjectItem> computing = new ArrayList<>();
        computing.add(new SubjectItem("COA", "Computer Organization and Architecture"));
        computing.add(new SubjectItem("DBMS", "Database Management Systems"));
        computing.add(new SubjectItem("DSA", "Data Structures and Algorithms"));
        computing.add(new SubjectItem("OOP", "Object Oriented Programming"));
        computing.add(new SubjectItem("OS", "Operating Systems"));
        computing.add(new SubjectItem("CN", "Computer Networks"));
        map.put("Computer Science", computing);
        map.put("Information Technology", new ArrayList<>(computing));

        List<SubjectItem> electronics = new ArrayList<>();
        electronics.add(new SubjectItem("DEC", "Digital Electronics"));
        electronics.add(new SubjectItem("VLSI", "VLSI Design"));
        electronics.add(new SubjectItem("EMFT", "Electromagnetic Field Theory"));
        electronics.add(new SubjectItem("DSP", "Digital Signal Processing"));
        map.put("Electronics", electronics);

        List<SubjectItem> mechanical = new ArrayList<>();
        mechanical.add(new SubjectItem("TOM", "Theory of Machines"));
        mechanical.add(new SubjectItem("FM", "Fluid Mechanics"));
        mechanical.add(new SubjectItem("HT", "Heat Transfer"));
        mechanical.add(new SubjectItem("CAD", "Computer Aided Design"));
        map.put("Mechanical", mechanical);

        List<SubjectItem> civil = new ArrayList<>();
        civil.add(new SubjectItem("SOM", "Strength of Materials"));
        civil.add(new SubjectItem("SA", "Structural Analysis"));
        civil.add(new SubjectItem("FM", "Fluid Mechanics"));
        civil.add(new SubjectItem("GEO", "Geotechnical Engineering"));
        map.put("Civil", civil);

        return map;
    }

    public static final class SubjectItem {
        private final String code;
        private final String name;

        public SubjectItem(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return code + " - " + name;
        }
    }
}
