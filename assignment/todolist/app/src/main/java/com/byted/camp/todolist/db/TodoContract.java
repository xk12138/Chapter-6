package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量

    public static final String CREATE =
            "CREATE TABLE " + NodeColumns.TABLE_NAME + " (" +
                    NodeColumns._ID + " INTEGER PRIMARY KEY," +
                    NodeColumns.NODE_STATE + " TEXT,"+
                    NodeColumns.NODE_DATE + " TEXT,"+
                    NodeColumns.NODE_CONTENT + " TEXT)";
    public static final String DELETE = "DROP TABLE IF EXISTS " + NodeColumns.TABLE_NAME;

    private TodoContract() {
    }

    public static class NodeColumns implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String NODE_STATE = "state";
        public static final String NODE_DATE = "date";
        public static final String NODE_CONTENT = "content";
    }

}
