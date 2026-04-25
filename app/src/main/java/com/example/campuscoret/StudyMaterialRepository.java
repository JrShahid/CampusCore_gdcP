package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public final class StudyMaterialRepository {
    private static final List<StudyMaterial> MATERIALS = buildSeededMaterials();

    private StudyMaterialRepository() {
    }

    public static void addMaterial(
            String title,
            String subjectName,
            String className,
            String fileType,
            String fileName,
            String uploadedBy
    ) {
        StudyMaterial material = new StudyMaterial(
                title,
                subjectName,
                className,
                fileType,
                fileName,
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

    private static List<StudyMaterial> buildSeededMaterials() {
        List<StudyMaterial> materials = new ArrayList<>();
        materials.add(new StudyMaterial("Unit 1 Lecture Notes", "Computer Science", "BCA 2B", "PDF", "cs_unit1_notes.pdf", "teacher@campus.edu", System.currentTimeMillis() - 86_400_000L));
        materials.add(new StudyMaterial("Boolean Algebra Slides", "Computer Science", "BCA 2B", "PPT", "boolean_algebra_intro.pptx", "teacher@campus.edu", System.currentTimeMillis() - 64_800_000L));
        materials.add(new StudyMaterial("Physics Lab Guide", "Physics", "BCA 2B", "PDF", "physics_lab_guide.pdf", "teacher@campus.edu", System.currentTimeMillis() - 43_200_000L));
        materials.add(new StudyMaterial("Calculus Worksheet", "Mathematics", "BCA 1A", "DOC", "calculus_practice.docx", "teacher@campus.edu", System.currentTimeMillis() - 21_600_000L));
        return materials;
    }

    static void replaceMaterialsFromFirebase(List<StudyMaterial> materials) {
        MATERIALS.clear();
        if (materials != null) {
            MATERIALS.addAll(materials);
        }
    }

    static List<StudyMaterial> exportMaterials() {
        return new ArrayList<>(MATERIALS);
    }
}
