package com.servet.loan;

import java.util.EnumMap;
import java.util.Map;

/** Spesifikasyondaki R5: başvurunun durum makinesi. */
public class LoanApplication {

    public enum Status {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PAID_OUT, CANCELLED
    }

    public enum Event {
        SUBMIT, START_REVIEW, APPROVE, REJECT, PAY_OUT, CANCEL
    }

    /** Durum tablosunun koddaki karşılığı: [mevcut durum][olay] → yeni durum. */
    private static final Map<Status, Map<Event, Status>> TRANSITIONS = new EnumMap<>(Status.class);

    static {
        allow(Status.DRAFT, Event.SUBMIT, Status.SUBMITTED);
        allow(Status.DRAFT, Event.CANCEL, Status.CANCELLED);
        allow(Status.SUBMITTED, Event.START_REVIEW, Status.UNDER_REVIEW);
        allow(Status.SUBMITTED, Event.CANCEL, Status.CANCELLED);
        allow(Status.UNDER_REVIEW, Event.APPROVE, Status.APPROVED);
        allow(Status.UNDER_REVIEW, Event.REJECT, Status.REJECTED);
        allow(Status.APPROVED, Event.PAY_OUT, Status.PAID_OUT);
     //   allow(Status.REJECTED, Event.SUBMIT, Status.SUBMITTED);
    }

    private static void allow(Status from, Event event, Status to) {
        TRANSITIONS.computeIfAbsent(from, s -> new EnumMap<>(Event.class)).put(event, to);
    }

    private Status status = Status.DRAFT;

    public Status status() {
        return status;
    }

    public void handle(Event event) {
        Status next = TRANSITIONS.getOrDefault(status, Map.of()).get(event);
        if (next == null) {
            throw new IllegalStateException("Geçersiz geçiş: " + status + " --" + event + "-->");
        }
        status = next;
    }
}