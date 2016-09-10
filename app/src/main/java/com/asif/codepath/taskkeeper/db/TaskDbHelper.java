package com.asif.codepath.taskkeeper.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.asif.codepath.taskkeeper.model.Task;
import com.asif.codepath.taskkeeper.utils.DateUtil;
import com.asif.codepath.taskkeeper.interfaces.ITaskListUpdateListener;
import com.asif.codepath.taskkeeper.db.TaskContract.TaskEntry;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Singleton class to manage the DB operations for task and
 * provides helper methods for task list management
 * Created by asif on 9/24/16.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    public final static String TAG = TaskDbHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TaskKeeper.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                    TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskEntry.COLUMN_NAME_TASK_ID + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_DETAIL + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_PRIORITY + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_DUE_DATE + TEXT_TYPE + COMMA_SEP +
                    TaskEntry.COLUMN_NAME_TASK_NOTES + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;

    private static TaskDbHelper sTaskDbHelper;
    private static ITaskListUpdateListener sTaskListUpdateListener;
    private ArrayList<Task> mTaskList;

    /**
     * Returns instance for the class
     * @param context
     * @return
     */
    public static TaskDbHelper getInstance(Context context){
        if(sTaskDbHelper == null){
            sTaskDbHelper = new TaskDbHelper(context);
        }
        return sTaskDbHelper;
    }

    /**
     * Constructor for class
     * @param context
     */
    private TaskDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mTaskList = new ArrayList<>();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "Deleting and creating table");
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void setTaskListUpdateListener(ITaskListUpdateListener listener){
        sTaskListUpdateListener = listener;
    }

    /**
     * Decides if its a udpate or new task and makes necessary operation.
     * @param task
     * @param isUpdate
     */
    public void addNewTask(Task task, boolean isUpdate){
        if(isUpdate){
            updateTask(task);
        }else {
            Log.d(TAG, "Inserting new task to DB "+ task.getTaskId());

            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_NAME_TASK_ID, task.getTaskId().toString());
            values.put(TaskEntry.COLUMN_NAME_DETAIL, task.getTaskName());
            values.put(TaskEntry.COLUMN_NAME_DUE_DATE, task.getSqlDate());
            values.put(TaskEntry.COLUMN_NAME_PRIORITY, task.getTaskPriority());
            values.put(TaskEntry.COLUMN_NAME_TASK_NOTES, task.getTaskNotes());

            db.insert(TaskEntry.TABLE_NAME, null, values);
            db.close();
            updateTaskList(null, null);
        }
    }

    /**
     * Update the task list by fetching the latest DB values
     * in ascending order (oldest to latest)
     */
    public void updateTaskList(String selection, String[] selectionArgs){
        Log.d(TAG, "Fetching all the tasks");
        mTaskList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_NAME_TASK_ID,
                TaskEntry.COLUMN_NAME_DETAIL,
                TaskEntry.COLUMN_NAME_DUE_DATE,
                TaskEntry.COLUMN_NAME_PRIORITY,
                TaskEntry.COLUMN_NAME_TASK_NOTES
        };

        String sortOrder =
                TaskEntry.COLUMN_NAME_DUE_DATE + " ASC";

        Cursor cursor = db.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            Task task = new Task();
            task.setTaskId(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_NAME_TASK_ID)));
            task.setTaskName(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_NAME_DETAIL)));
            task.setSqlDate(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_NAME_DUE_DATE)));
            task.setDate(DateUtil.getDisplayDate(task.getSqlDate()));
            task.setTaskPriority(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_NAME_PRIORITY)));
            task.setTaskNotes(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_NAME_TASK_NOTES)));

            mTaskList.add(task);
        }
        cursor.close();
    }

    /**
     * Updates a existing task
     * @param task
     * @return
     */
    private boolean updateTask(Task task){

        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_DETAIL, task.getTaskName());
        values.put(TaskEntry.COLUMN_NAME_PRIORITY, task.getTaskPriority());
        values.put(TaskEntry.COLUMN_NAME_DUE_DATE, task.getSqlDate());
        values.put(TaskEntry.COLUMN_NAME_TASK_NOTES, task.getTaskNotes());

        String selection = TaskEntry.COLUMN_NAME_TASK_ID + " = ?";
        String[] selectionArgs = { task.getTaskId().toString() };

        int count = db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        if (count > 0){
            updateTaskList(null, null);
            return true;
        }
        return false;
    }

    /**
     * Deletes a task
     * @param task
     * @return
     */
    public boolean deleteTask(Task task){
        SQLiteDatabase db = getWritableDatabase();
        String selection = TaskEntry.COLUMN_NAME_TASK_ID + " = ?";
        String[] selectionArgs = { task.getTaskId().toString() };
        int count = db.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
        if(count > 0){
            updateTaskList(null, null);
            return true;
        }
        return false;
    }

    /**
     * Returns the most updated task list
     * @return
     */
    public ArrayList<Task> getTaskList(){
        return mTaskList;
    }

    public Task getTaskById(UUID taskId){
        for(Task task : mTaskList){
            if(task.getTaskId().equals(taskId)){
                return task;
            }
        }
        return null;
    }

    /**
     * Calls listener whenever new update for task list is available
     */
    public void callTaskListUpdateListener(){
        sTaskListUpdateListener.onTaskListUpdate();
    }

}
