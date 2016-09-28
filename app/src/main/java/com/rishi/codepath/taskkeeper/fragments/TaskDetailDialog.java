package com.rishi.codepath.taskkeeper.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rishi.codepath.taskkeeper.interfaces.IDatePickerDialogListener;
import com.rishi.codepath.taskkeeper.R;
import com.rishi.codepath.taskkeeper.model.Task;
import com.rishi.codepath.taskkeeper.db.TaskDbHelper;
import com.rishi.codepath.taskkeeper.model.TaskPriority;
import com.rishi.codepath.taskkeeper.utils.DateUtil;

import java.util.UUID;

/**
 * Shows dialog to fill in task details
 * Created by rishi on 9/23/16.
 */
public class TaskDetailDialog extends DialogFragment {
    private static final String EXTRA_TASK_ID = "com.rishi.codepath.simpletodo.taskId";
    private RadioGroup mRadioGroup;
    private TextView mTvDueDate;
    private EditText mEtTask;
    private EditText mEtTaskNotes;
    private DatePickerFragment mDatePickerFragment;
    private Task mCurrentTask;
    private boolean mIsUpdate = false;

    /**
     * Returns new instance
     * @return
     */
    public static TaskDetailDialog newInstance(Task task) {
        TaskDetailDialog fragment = new TaskDetailDialog();
        Bundle extras = new Bundle();
        extras.putString(EXTRA_TASK_ID, task.getTaskId().toString());
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_task_details, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UUID taskId = UUID.fromString(getArguments().getString(EXTRA_TASK_ID));
        mCurrentTask = TaskDbHelper.getInstance(getContext()).getTaskById(taskId);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group_priority);
        mTvDueDate = (TextView) view.findViewById(R.id.text_due_date_value);
        mEtTask = (EditText) view.findViewById(R.id.edit_task);
        final Button btnDueDate = (Button) view.findViewById(R.id.button_date_select);
        mEtTaskNotes = (EditText) view.findViewById(R.id.edit_task_notes);

        if(mCurrentTask != null){
            mIsUpdate = true;
            ((RadioButton) mRadioGroup.findViewById(TaskPriority.getRadioOptionForValue(mCurrentTask.getTaskPriority()))).setChecked(true);
            mEtTask.setText(mCurrentTask.getTaskName());
            mTvDueDate.setText(mCurrentTask.getDate());
            mEtTaskNotes.setText(mCurrentTask.getTaskNotes());

        }else {
            mCurrentTask = new Task();
            mCurrentTask.setTaskId(taskId.toString());
            mEtTask.setText(mCurrentTask.getTaskName());
        }

        mTvDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDueDate.callOnClick();
            }
        });
        view.findViewById(R.id.btnSaveTask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid()){
                    sendResult(true);
                    getDialog().dismiss();
                }else {
                    mEtTask.setError(getString(R.string.task_required_error));
                }
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(false);
                getDialog().dismiss();

            }
        });

        mDatePickerFragment = DatePickerFragment.newInstance(getContext(), R.string.title_date_dialog);
        mDatePickerFragment.setDatePickerDialogListener(new IDatePickerDialogListener() {
            @Override
            public void setDate(int year, int month, int day) {
                mTvDueDate.setText(DateUtil.getDisplayDate(year, month, day));
            }
        });
        btnDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        getDialog().setTitle("Task details");
        // Show soft keyboard automatically and request focus to field
        mEtTask.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void sendResult(boolean isSave) {
        if(isSave){
            mCurrentTask.setTaskName(mEtTask.getText().toString());
            mCurrentTask.setDate(mTvDueDate.getText().toString());
            mCurrentTask.setSqlDate(DateUtil.getSQLDate(mTvDueDate.getText().toString()));
            mCurrentTask.setTaskNotes(mEtTaskNotes.getText().toString());
            mCurrentTask.setTaskPriority(TaskPriority.getValueForOption(mRadioGroup.getCheckedRadioButtonId()));
            addNewTask();
        }
    }

    /**
     * Task name is required field, returns true if
     * it is filled
     * @return
     */
    private boolean isValid(){
        if(mEtTask.getText().toString().equalsIgnoreCase("")){
            return false;
        }
        return true;
    }

    /**
     * Async task for adding new task
     */
    private void addNewTask(){
        AsyncTask addTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                TaskDbHelper.getInstance(getContext()).addNewTask(mCurrentTask, mIsUpdate);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                TaskDbHelper.getInstance(getContext()).callTaskListUpdateListener();

            }
        };
        addTask.execute();
    }
}
