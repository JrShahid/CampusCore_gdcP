package com.example.campuscoret;

public class TimetableEntry {
    private final String className;
    private final String subjectName;
    private final String building;
    private final String floor;
    private final String roomNumber;
    private final int slotOrder;

    public TimetableEntry(
            String className,
            String subjectName,
            String building,
            String floor,
            String roomNumber,
            int slotOrder
    ) {
        this.className = className;
        this.subjectName = subjectName;
        this.building = building;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.slotOrder = slotOrder;
    }

    public String getClassName() {
        return className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public int getSlotOrder() {
        return slotOrder;
    }
}
