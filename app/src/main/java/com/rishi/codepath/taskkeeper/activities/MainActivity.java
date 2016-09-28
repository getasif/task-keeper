package com.rishi.codepath.taskkeeper.activities;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.rishi.codepath.taskkeeper.R;
import com.rishi.codepath.taskkeeper.adapter.ItemAdapter;
import com.rishi.codepath.taskkeeper.fragments.FilterTaskDialog;
import com.rishi.codepath.taskkeeper.fragments.TaskDetailDialog;
import com.rishi.codepath.taskkeeper.interfaces.ITaskListUpdateListener;
import com.rishi.codepath.taskkeeper.model.Task;
import com.rishi.codepath.taskkeeper.db.TaskDbHelper;

import java.util.ArrayList;

/**
 * Main view for the app
 *
 */
public class MainActivity extends AppCompatActivity implements ITaskListUpdateListener {

    private ArrayList<Task> mItems;
    private ItemAdapter mItemsAdapter;
    private ListView mLvItems;
    private TaskDbHelper mTaskDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLvItems = (ListView) findViewById(R.id.list_items);
        mTaskDbHelper = TaskDbHelper.getInstance(this);
        mTaskDbHelper.setTaskListUpdateListener(this);
        getTaskList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_filter:
                FragmentManager fm = getSupportFragmentManager();
                FilterTaskDialog dialogFragment = new FilterTaskDialog();
                dialogFragment.show(fm, "dialog_fragment_filter");
                return true;
            case R.id.menu_refresh:
                getTaskList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupAdapter(){
        mItemsAdapter = new ItemAdapter(this, mItems, getSupportFragmentManager());
        mLvItems.setAdapter(mItemsAdapter);
    }

    /**
     * Reads items from file to display
     */
    private void fetchTasks(){
        mItems = mTaskDbHelper.getTaskList();
        setupAdapter();
        if(mItems.size() > 0){
            (findViewById(R.id.text_no_item)).setVisibility(View.GONE);
        }else {
            (findViewById(R.id.text_no_item)).setVisibility(View.VISIBLE);
        }
    }

    /**
     * On click method for Add button
     * @param view
     */
    public void onAddItem(View view){
        FragmentManager fm = getSupportFragmentManager();
        TaskDetailDialog dialogFragment = TaskDetailDialog.newInstance(new Task());
        dialogFragment.show(fm, "fragment_edit_name");
    }


    @Override
    public void onTaskListUpdate() {
        fetchTasks();
    }

    /**
     * Get updated task list
     */
    private void getTaskList(){
        AsyncTask getTasks = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mTaskDbHelper.updateTaskList(null, null);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                fetchTasks();
            }
        };
        getTasks.execute();
    }
}
