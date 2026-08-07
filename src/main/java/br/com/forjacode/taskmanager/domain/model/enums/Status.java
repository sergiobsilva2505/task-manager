package br.com.forjacode.taskmanager.domain.model.enums;

public enum Status {
    TODO, IN_PROGRESS, DONE, CANCELLED;

    public boolean canTransitionTo(Status next) {
        return switch (this) {
            case TODO -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == DONE || next == CANCELLED;
            case DONE, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return this == DONE || this == CANCELLED;
    }
}
