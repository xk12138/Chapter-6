package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String TAG = "MainActivity";
    private TodoDbHelper todoDbHelper = new TodoDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        List<Note> notes = new LinkedList<>();

        SQLiteDatabase db = todoDbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                TodoContract.NodeColumns.NODE_DATE,
                TodoContract.NodeColumns.NODE_STATE,
                TodoContract.NodeColumns.NODE_CONTENT,
        };


        //select * from :TABLE_NAME;
        Cursor cursor = db.query(TodoContract.NodeColumns.TABLE_NAME, projection, null, null, null, null, null);

        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.NodeColumns._ID));
            String sDate = cursor.getString(cursor.getColumnIndex(TodoContract.NodeColumns.NODE_DATE));
            int state = cursor.getInt(cursor.getColumnIndex(TodoContract.NodeColumns.NODE_STATE));
            String content = cursor.getString(cursor.getColumnIndex(TodoContract.NodeColumns.NODE_CONTENT));

            Note node = new Note(itemId);
            Date date = null;
            try {
                date = dateFormat.parse(sDate);
            }
            catch (ParseException e) { }

            node.setDate(date);
            node.setState(State.from(state));
            node.setContent(content);
            notes.add(node);
        }

        Log.d(TAG, "成功获取" + notes.size() + "行数据");

        cursor.close();
        todoDbHelper.close();
        return notes;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = todoDbHelper.getWritableDatabase();

        String whereClause = TodoContract.NodeColumns.NODE_CONTENT + " like ? ";
        String[] args = {note.getContent()};

        // delete from :TABLE_NAME where :NODE_CONTENT like :CONTENT;
        int deletedRows = db.delete(TodoContract.NodeColumns.TABLE_NAME, whereClause, args);
        Log.d(TAG, "成功删除了" + deletedRows + "行数据");
        todoDbHelper.close();

        //没有重新绘制，但是成功删除了
    }

    private void updateNode(Note note) {
        // 更新数据
        SQLiteDatabase db = todoDbHelper.getWritableDatabase();
        int state = 0;      //记录是否发生变化
        ContentValues values = new ContentValues();
        values.put(TodoContract.NodeColumns.NODE_STATE, state);
        String whereClause = TodoContract.NodeColumns.NODE_DATE + " like ? ";
        String[] args = {dateFormat.format(note.getDate())};

        //update :TABLE_NAME set NODE_STATE=:state where :NODE_DATE like :date;
        int count = db.update(TodoContract.NodeColumns.TABLE_NAME, values, whereClause, args);
        Log.d(TAG, "成功更新了" + count + "条数据");
        todoDbHelper.close();
    }

}
