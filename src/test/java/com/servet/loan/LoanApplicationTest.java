package com.servet.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.servet.loan.LoanApplication.Event;
import com.servet.loan.LoanApplication.Status;

@DisplayName("LoanApplication — R5 State Transition")
class LoanApplicationTest {

    /** Her duruma sadece GEÇERLİ olaylarla ulaşmanın yolu (arka kapı yok). */
    private static final Map<Status, List<Event>> PATH_TO = Map.of(
            Status.DRAFT, List.of(),
            Status.SUBMITTED, List.of(Event.SUBMIT),
            Status.UNDER_REVIEW, List.of(Event.SUBMIT, Event.START_REVIEW),
            Status.APPROVED, List.of(Event.SUBMIT, Event.START_REVIEW, Event.APPROVE),
            Status.REJECTED, List.of(Event.SUBMIT, Event.START_REVIEW, Event.REJECT),
            Status.PAID_OUT, List.of(Event.SUBMIT, Event.START_REVIEW, Event.APPROVE, Event.PAY_OUT),
            Status.CANCELLED, List.of(Event.CANCEL));

    /** Durum tablosundaki 7 geçerli geçiş. */
    private static final Set<String> VALID = Set.of(
            "DRAFT:SUBMIT", "DRAFT:CANCEL", "SUBMITTED:START_REVIEW", "SUBMITTED:CANCEL",
            "UNDER_REVIEW:APPROVE", "UNDER_REVIEW:REJECT", "APPROVED:PAY_OUT");

    private static LoanApplication applicationIn(Status status) {
        LoanApplication application = new LoanApplication();
        PATH_TO.get(status).forEach(application::handle);
        assertThat(application.status()).isEqualTo(status);
        return application;
    }

    @Test
    @DisplayName("Yeni başvuru DRAFT durumunda başlar")
    void startsAsDraft() {
        assertThat(new LoanApplication().status()).isEqualTo(Status.DRAFT);
    }

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @CsvSource({
            "DRAFT,        SUBMIT,       SUBMITTED",
            "DRAFT,        CANCEL,       CANCELLED",
            "SUBMITTED,    START_REVIEW, UNDER_REVIEW",
            "SUBMITTED,    CANCEL,       CANCELLED",
            "UNDER_REVIEW, APPROVE,      APPROVED",
            "UNDER_REVIEW, REJECT,       REJECTED",
            "APPROVED,     PAY_OUT,      PAID_OUT"
    })
    @DisplayName("Geçerli geçişler (all transitions coverage)")
    void validTransitions(Status from, Event event, Status expected) {
        LoanApplication application = applicationIn(from);

        application.handle(event);

        assertThat(application.status()).isEqualTo(expected);
    }

    /** Durum tablosundaki 42 hücreden geçerli olmayan 35'i otomatik üretilir. */
    static Stream<Arguments> invalidTransitions() {
        List<Arguments> invalid = new ArrayList<>();
        for (Status status : Status.values()) {
            for (Event event : Event.values()) {
                if (!VALID.contains(status + ":" + event)) {
                    invalid.add(Arguments.of(status, event));
                }
            }
        }
        return invalid.stream();
    }

    @ParameterizedTest(name = "{0} --{1}--> ❌")
    @MethodSource("invalidTransitions")
    @DisplayName("Geçersiz geçişler reddedilir ve durum değişmez")
    void invalidTransitionsAreRejected(Status from, Event event) {
        LoanApplication application = applicationIn(from);

        assertThatIllegalStateException().isThrownBy(() -> application.handle(event))
                .withMessageContaining(from.name());
        assertThat(application.status()).isEqualTo(from);
    }

    @Test
    @DisplayName("Durum tablosu eksiksiz: 7 geçerli + 35 geçersiz = 42 hücre")
    void stateTableIsComplete() {
        assertThat(VALID).hasSize(7);
        assertThat(invalidTransitions()).hasSize(35);
        assertThat(Status.values().length * Event.values().length).isEqualTo(42);
    }

    @Test
    @DisplayName("Uçtan uca mutlu yol: DRAFT → PAID_OUT")
    void happyPath() {
        LoanApplication application = new LoanApplication();

        List.of(Event.SUBMIT, Event.START_REVIEW, Event.APPROVE, Event.PAY_OUT).forEach(application::handle);

        assertThat(application.status()).isEqualTo(Status.PAID_OUT);
    }
}