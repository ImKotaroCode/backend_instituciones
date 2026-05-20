package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTaskGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseTaskGroupMemberRepository extends JpaRepository<CourseTaskGroupMember, Long> {
    List<CourseTaskGroupMember> findByGroupId(Long groupId);
    List<CourseTaskGroupMember> findByGroupIdIn(List<Long> groupIds);
    List<CourseTaskGroupMember> findByGroupIdInAndStudentId(List<Long> groupIds, Long studentId);
    List<CourseTaskGroupMember> findByStudentId(Long studentId);
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);

    @Modifying
    @Query("DELETE FROM CourseTaskGroupMember m WHERE m.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM CourseTaskGroupMember m WHERE m.groupId IN :groupIds")
    void deleteByGroupIdIn(@Param("groupIds") List<Long> groupIds);
}
