package de.fh_zwickau.pti.geobe.dto

/**
 * Created by georg beier on 16.11.2015.
 */
class SprintDto {
    public static class QList {
        LinkedHashMap<Long, QNode> all = [:]

        Long getFirstId() {
            if (all) {
                all.keySet().iterator().next()
            } else {
                0
            }
        }
    }

    public static class QFull {
        Long id = 0
        String name = ''
        Date start
        Date end
        ProjectDto.QNode project = new ProjectDto.QNode()
        def backlog = new TaskDto.QList()
//        def available = new TaskDto.QList()
    }

    public static class CSet {
        Long id = 0
        Long projectId = 0
        String name = ''
        Date start
        Date end
        List<Long> taskIds = []
    }

    public static class QNode {
        Long id
        String name
        def backlog = new TaskDto.QList()
//        def available = new TaskDto.QList()

        @Override
        String toString() {
            this.@name
        }
    }
}
