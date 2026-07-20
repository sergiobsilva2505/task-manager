package br.com.forjacode.taskmanager.application.ports.shared;

public enum TaskSortField {
    TITLE,
    CREATED_AT,
    DUE_DATE,
    PRIORITY;

    public static final TaskSortField DEFAULT = CREATED_AT;
}

