package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public final class StudyMaterialRepository {
    private static final List<StudyMaterial> MATERIALS = new ArrayList<>();

    private StudyMaterialRepository() {
    }

    public static void addMaterial(
            String title,
            String subjectName,
            String className,
            String fileType,
            String fileName,
            String fileUrl,
            String uploadedBy
    ) {
        StudyMaterial material = new StudyMaterial(
                title,
                subjectName,
                className,
                fileType,
                fileName,
                fileUrl,
                uploadedBy,
                System.currentTimeMillis()
        );
        MATERIALS.add(0, material);
        FirebaseCampusSync.publishStudyMaterial(material);
    }

    public static List<StudyMaterial> getMaterialsForClass(String className) {
        List<StudyMaterial> results = new ArrayList<>();
        for (StudyMaterial material : MATERIALS) {
            if (material.getClassName().equals(className)) {
                results.add(material);
            }
        }
        return results;
    }

    public static List<StudyMaterial> getMaterialsForClassAndSubject(String className, String subjectName) {
        List<StudyMaterial> results = new ArrayList<>();
        for (StudyMaterial material : MATERIALS) {
            if (material.getClassName().equals(className)
                    && material.getSubjectName().equals(subjectName)) {
                results.add(material);
            }
        }
        return results;
    }

    static void replaceMaterialsFromFirebase(List<StudyMaterial> materials) {
        MATERIALS.clear();
        if (materials != null) {
            MATERIALS.addAll(materials);
        }
    }

}
