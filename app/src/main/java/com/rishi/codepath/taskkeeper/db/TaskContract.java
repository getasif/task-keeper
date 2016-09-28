package com.rishi.codepath.taskkeeper.db;

import android.provider.BaseColumns;

/**
 * Contract for Favourite restaurants database
 * Created by rishi on 9/24/16.
 */
public class TaskContract {
    public TaskContract(){}

    public static abstract class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_TASK_ID = "task_id";
        public static final String COLUMN_NAME_DETAIL = "detail";
        public static final String COLUMN_NAME_PRIORITY = "priority";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
        public static final String COLUMN_NAME_TASK_NOTES = "task_notes";
    }
}
