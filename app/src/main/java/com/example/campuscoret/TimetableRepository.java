package com.example.campuscoret;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class TimetableRepository {
    private static final List<TimetableEntry> ENTRIES = buildEntries();

    private TimetableRepository() {
    }

    public static TimetableEntry findBySubject(String className, String subjectName) {
        for (TimetableEntry entry : ENTRIES) {
            if (entry.getClassName().equals(className)
                    && entry.getSubjectName().equals(subjectName)) {
                return entry;
            }
        }
        return null;
    }

    public static TimetableEntry findNextClass(String className) {
        int currentSlot = resolveCurrentSlot();
        TimetableEntry candidate = null;

        for (TimetableEntry entry : ENTRIES) {
            if (!entry.getClassName().equals(className)) {
                continue;
            }

            if (entry.getSlotOrder() >= currentSlot) {
                if (candidate == null || entry.getSlotOrder() < candidate.getSlotOrder()) {
                    candidate = entry;
                }
            }
        }

        if (candidate != null) {
            return candidate;
        }

        for (TimetableEntry entry : ENTRIES) {
            if (entry.getClassName().equals(className)) {
                return entry;
            }
        }

        return null;
    }

    public static void upsertEntry(
            String className,
            String subjectName,
            String building,
            String floor,
            String roomNumber,
            int slotOrder
    ) {
        for (int i = 0; i < ENTRIES.size(); i++) {
            TimetableEntry entry = ENTRIES.get(i);
            if (entry.getClassName().equals(className)
                    && entry.getSubjectName().equals(subjectName)) {
                ENTRIES.set(i, new TimetableEntry(className, subjectName, building, floor, roomNumber, slotOrder));
                return;
            }
        }

        ENTRIES.add(new TimetableEntry(className, subjectName, building, floor, roomNumber, slotOrder));
    }

    private static int resolveCurrentSlot() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 10) {
            return 1;
        }
        if (hour < 12) {
            return 2;
        }
        if (hour < 14) {
            return 3;
        }
        if (hour < 16) {
            return 4;
        }
        return 5;
    }

    private static List<TimetableEntry> buildEntries() {
        List<TimetableEntry> entries = new ArrayList<>();
        entries.add(new TimetableEntry("BCA 1A", "Mathematics", "Academic Block A", "Floor 1", "A-104", 1));
        entries.add(new TimetableEntry("BCA 1A", "English", "Language Wing", "Floor 2", "L-203", 3));
        entries.add(new TimetableEntry("BCA 2B", "Computer Science", "Technology Block", "Floor 3", "T-307", 2));
        entries.add(new TimetableEntry("BCA 2B", "Physics", "Science Block", "Floor 1", "S-112", 4));
        entries.add(new TimetableEntry("BSc CS 3A", "Computer Science", "Innovation Centre", "Floor 4", "I-409", 2));
        entries.add(new TimetableEntry("BSc CS 3A", "Mathematics", "Academic Block B", "Floor 2", "B-218", 5));
        entries.add(new TimetableEntry("MCA 1C", "Physics", "Research Wing", "Floor 2", "R-205", 1));
        entries.add(new TimetableEntry("MCA 1C", "Computer Science", "Technology Block", "Floor 5", "T-512", 3));
        return entries;
    }
}
