package de.fh_zwickau.pti.geobe.dto

/**
 * Created by georg beier on 16.11.2015.
 */
class TaskDto {
    public static class QList {
        LinkedHashMap<Long, String> all = [:]

        Long getFirstId() {
            if (all) {
                all.keySet().iterator().next()
            } else {
                0
            }
        }
    }

    public static class QFull {
        Long id
        String classname
        String tag
        String description
        Long estimate
        Long summedEstimate
        Long spent
        Boolean completed
        ProjectDto.QList project = new ProjectDto.QList()
        SprintDto.QList sprints = new SprintDto.QList()
        QList supertask = new QList()
        List<QNode> subtasks = []
    }

    public static class QNode {
        Long id
        String tag
        List<QNode> children = []
    }

    public static class CSet {
        Long id = 0
        String classname
        String tag
        String description
        Long estimate
        Long spent
        Boolean completed
        Long projectId
        List<Long> sprintIds = []
        Long supertaskId
        List<Long> subtaskIds = []
    }
}
