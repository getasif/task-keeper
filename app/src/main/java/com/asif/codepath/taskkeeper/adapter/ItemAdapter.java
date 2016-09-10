package com.asif.codepath.taskkeeper.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asif.codepath.taskkeeper.R;
import com.asif.codepath.taskkeeper.db.TaskDbHelper;
import com.asif.codepath.taskkeeper.fragments.TaskDetailDialog;
import com.asif.codepath.taskkeeper.model.Task;
import com.asif.codepath.taskkeeper.model.TaskPriority;

import java.util.ArrayList;

/**
 * Adapter for list view of tasks. Handles click and long clicks
 * Created by asif on 9/23/16.
 */
public class ItemAdapter extends ArrayAdapter<Task> {

    private Context mContext;
    private Activity mActivity;
    private FragmentManager mFragmentSupportManager;
    private ActionMode mActionMode;
    private Task mSelectedTask;
    private View mSelectedView;

    /**
     * Holder for better performance of the list view
     */
    private static class ViewHolder {
        TextView task_detail;
        TextView due_date;
        ImageView priority;
    }

    /**
     * Constructor for the adapter
     * @param activity
     * @param items
     * @param fragmentManager
     */
    public ItemAdapter(Activity activity, ArrayList<Task> items, FragmentManager fragmentManager) {
        super(activity.getApplicationContext(), R.layout.item_task, items);

        mActivity = activity;
        mContext = activity.getApplicationContext();
        mFragmentSupportManager = fragmentManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Task task = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_task, parent, false);
            viewHolder.task_detail = (TextView) convertView.findViewById(R.id.text_task);
            viewHolder.due_date = (TextView) convertView.findViewById(R.id.text_due_date);
            viewHolder.priority = (ImageView) convertView.findViewById(R.id.image_priority);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.task_detail.setText(task.getTaskName());
        viewHolder.due_date.setText(task.getDisplayDate());
        viewHolder.priority.setImageDrawable(mContext.getResources().getDrawable(TaskPriority.getIconForValue(task.getTaskPriority())));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActionMode != null){
                    mActionMode.finish();
                }
                FragmentManager fm = mFragmentSupportManager;
                TaskDetailDialog dialogFragment = TaskDetailDialog.newInstance(task);
                dialogFragment.show(fm, "fragment_edit_task");
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mSelectedView = view;
                if (mActionMode != null) {
                    return false;
                }

                mSelectedTask = task;
                mActionMode = mActivity.
                        startActionMode(mActionModeCallback);
                mSelectedView.setSelected(true);
                return true;
            }
        });

        return convertView;

    }

    /**
     * To show the contextual action bar for delete task option
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    if(mSelectedTask != null){
                        AsyncTask task = new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] objects) {
                                TaskDbHelper.getInstance(mContext).deleteTask(mSelectedTask);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                TaskDbHelper.getInstance(mContext).callTaskListUpdateListener();
                            }
                        };
                        task.execute();
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelectedView.setSelected(false);

        }
    };
}
