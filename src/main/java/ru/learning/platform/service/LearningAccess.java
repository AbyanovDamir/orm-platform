package ru.learning.platform.service;

import org.springframework.stereotype.Component;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;

import static ru.learning.platform.service.PlatformException.*;

@Component
class LearningAccess {
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    LearningAccess(UserRepository users, EnrollmentRepository enrollments) {
        this.users = users;
        this.enrollments = enrollments;
    }

    User student(Long id) {
        User user = require(users.findById(id), "Пользователь", id);
        check(user.getRole() == Role.STUDENT, Kind.FORBIDDEN, "Действие доступно студенту");
        return user;
    }

    void requireActive(Long courseId, Long studentId) {
        check(enrollments.findByCourseIdAndStudentId(courseId, studentId)
                        .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE).isPresent(),
                Kind.FORBIDDEN, "Нужна активная запись на курс");
    }
}
