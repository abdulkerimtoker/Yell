package yell.client.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yell.client.type.Message;
import yell.client.util.DateFormat;

/**
 * Created by abdulkerim on 09.05.2016.
 */
public class ConversationSqlHelper extends SQLiteOpenHelper {

    public ConversationSqlHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COLUMN_NAME_MESSAGE_TEXT + " TEXT, " +
                COLUMN_NAME_MESSAGE_TYPE + " TEXT, " +
                COLUMN_NAME_DATE + " TEXT, " +
                COLUMN_NAME_IS_SENT_BY_THIS + " INT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertMessage(Message message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_MESSAGE_TEXT, message.message);
        values.put(COLUMN_NAME_DATE, message.date);
        values.put(COLUMN_NAME_MESSAGE_TYPE, message.messageType);
        values.put(COLUMN_NAME_IS_SENT_BY_THIS, message.isSentByThis ? 1 : 0);

        db.insert(TABLE_NAME, null, values);
    }

    public List<Message> getAllMessages(boolean ascending) {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COLUMN_NAME_MESSAGE_TEXT,
                COLUMN_NAME_MESSAGE_TYPE,
                COLUMN_NAME_DATE,
                COLUMN_NAME_IS_SENT_BY_THIS
        };

        Cursor result = db.query(TABLE_NAME, columns, null, null, null, null,
                COLUMN_NAME_DATE + (ascending ? " ASC" : " DESC"));

        ArrayList<Message> list = new ArrayList<>(result.getCount() + 10);

        int messageColumnIndex      = result.getColumnIndex(COLUMN_NAME_MESSAGE_TEXT);
        int messageTypeColumnIndex  = result.getColumnIndex(COLUMN_NAME_MESSAGE_TYPE);
        int dateColumnIndex         = result.getColumnIndex(COLUMN_NAME_DATE);
        int isSentByThisColumnIndex = result.getColumnIndex(COLUMN_NAME_IS_SENT_BY_THIS);

        result.moveToFirst();

        for (int i = 0; i < result.getCount(); i++) {
            boolean isSentByThis = result.getInt(isSentByThisColumnIndex) == 1;

            Message message = new Message(isSentByThis,
                    result.getString(messageColumnIndex),
                    result.getString(messageTypeColumnIndex),
                    result.getString(dateColumnIndex));

            list.add(message);
            result.moveToNext();
        }

        return list;
    }

    public static final String TABLE_NAME = "Message";
    public static final String COLUMN_NAME_MESSAGE_TEXT = "message_text";
    public static final String COLUMN_NAME_MESSAGE_TYPE = "message_type";
    public static final String COLUMN_NAME_DATE = "message_date";
    public static final String COLUMN_NAME_IS_SENT_BY_THIS = "is_sent_by_this";
}
