package com.asif.codepath.taskkeeper.fragments;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.asif.codepath.taskkeeper.R;
import com.asif.codepath.taskkeeper.db.TaskContract;
import com.asif.codepath.taskkeeper.db.TaskDbHelper;
import com.asif.codepath.taskkeeper.utils.DateUtil;

/**
 * Filter option for filtering the tasks
 * Created by asif on 9/23/16.
 */
public class FilterTaskDialog extends DialogFragment {

    private TaskDbHelper mTaskDbHelper;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_filter_options, null);

        mTaskDbHelper = TaskDbHelper.getInstance(getActivity());
        final RadioGroup filterOptions = (RadioGroup) v.findViewById(R.id.radio_group_filter);


        (v.findViewById(R.id.diaglog_search)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                configureFilter(filterOptions.getCheckedRadioButtonId());
            }
        });

        (v.findViewById(R.id.diaglog_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                configureFilter(-1);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Filter tasks")
                .create();
    }

    private void configureFilter(int optionId) {
        if(optionId == -1){
            return;
        }

        String dueDateSelection = TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE;
        String prioritySelection = TaskContract.TaskEntry.COLUMN_NAME_PRIORITY + " = ?";;
        String[] dateArgs = { DateUtil.getCurrentSQLDate() };
        switch (optionId){
            case R.id.radio_overdue:
                dueDateSelection = dueDateSelection + " < ?";
                filterTasks(dueDateSelection, dateArgs);
                break;
            case R.id.radio_today:
                dueDateSelection = dueDateSelection + " = ?";
                filterTasks(dueDateSelection, dateArgs);
                break;
            case R.id.radio_tomorrow:
                dueDateSelection = dueDateSelection + " > ?";
                filterTasks(dueDateSelection, dateArgs);
                break;
            case R.id.radio_high:
                String[] priorityArgs = {"2"};
                filterTasks(prioritySelection, priorityArgs);
                break;
            default:
                break;
        }
    }

    /**
     * Async task for fetching the filtered result and updating the view by
     * calling the listener
     * @param selection
     * @param selectionArgs
     */
    private void filterTasks(final String selection, final String[] selectionArgs){
        AsyncTask filterTasks = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mTaskDbHelper.updateTaskList(selection, selectionArgs);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                mTaskDbHelper.callTaskListUpdateListener();
            }
        };
        filterTasks.execute();
    }
}
