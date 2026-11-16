package ru.learning.platform.service;

import ru.learning.platform.entity.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.learning.platform.service.PlatformException.*;

final class QuizScoring {
    record QuestionKey(Long id, QuestionType type, Set<Long> options, Set<Long> correct) {
    }

    private QuizScoring() {
    }

    static int score(List<QuestionKey> keys, Map<Long, Set<Long>> answers) {
        check(answers.size() == keys.size(), Kind.BAD_INPUT, "Ответьте на каждый вопрос теста");
        int score = 0;
        for (QuestionKey key : keys) {
            Set<Long> selected = answers.get(key.id());
            check(selected != null && !selected.isEmpty() && key.options().containsAll(selected), Kind.BAD_INPUT,
                    "Проверьте варианты ответа на вопрос №" + key.id());
            check(key.type() != QuestionType.SINGLE_CHOICE || selected.size() == 1, Kind.BAD_INPUT,
                    "Для вопроса №" + key.id() + " выберите один вариант");
            if (selected.equals(key.correct())) {
                score++;
            }
        }
        return score;
    }
}
