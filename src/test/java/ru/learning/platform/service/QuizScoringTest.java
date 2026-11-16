package ru.learning.platform.service;

import org.junit.jupiter.api.Test;
import ru.learning.platform.entity.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static ru.learning.platform.service.QuizScoring.*;

class QuizScoringTest {
    private final QuestionKey single = new QuestionKey(1L, QuestionType.SINGLE_CHOICE, Set.of(11L, 12L), Set.of(11L));
    private final QuestionKey multiple = new QuestionKey(2L, QuestionType.MULTIPLE_CHOICE,
            Set.of(21L, 22L, 23L), Set.of(21L, 22L));

    @Test
    void awardsOnePointForEachExactSet() {
        assertThat(score(List.of(single, multiple), Map.of(1L, Set.of(11L), 2L, Set.of(22L, 21L)))).isEqualTo(2);
    }

    @Test
    void givesNoPartialCredit() {
        assertThat(score(List.of(multiple), Map.of(2L, Set.of(21L)))).isZero();
        assertThat(score(List.of(multiple), Map.of(2L, Set.of(21L, 22L, 23L)))).isZero();
    }

    @Test
    void rejectsOptionsFromAnotherQuestion() {
        assertThatThrownBy(() -> score(List.of(single), Map.of(1L, Set.of(21L)))).isInstanceOf(PlatformException.class);
    }

    @Test
    void rejectsMissingAnswersAndMultipleSelectionsInSingleChoice() {
        assertThatThrownBy(() -> score(List.of(single), Map.of())).isInstanceOf(PlatformException.class);
        assertThatThrownBy(() -> score(List.of(single), Map.of(1L, Set.of(11L, 12L))))
                .isInstanceOf(PlatformException.class);
    }
}
