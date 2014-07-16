package com.cedar.autologin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class SQLiteHelper extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "log";
 
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_LOG_TABLE = "CREATE TABLE log ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "date TEXT, "+
                "msg TEXT )";
 
        // create books table
        db.execSQL(CREATE_LOG_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        //db.execSQL("DROP TABLE IF EXISTS log");
 
        // create fresh books table
        //this.onCreate(db);
    }

    private static final String TABLE_LOG = "log";

    private static final String ID = "id";
    private static final String DATE = "date";
    private static final String MSG = "msg";
 
    private static final String[] COLUMNS = {ID, DATE, MSG};
 
    public void addLog(String log){
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        java.util.Date d = Calendar.getInstance().getTime();
        String dateStamp = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(d);
        String timeStamp = new SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(d);
        values.put(DATE, dateStamp); // get title 
        values.put(MSG, timeStamp + "\t\t" + log); // get author
 
        db.insert(TABLE_LOG, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
        //Log.d("addLog", log + dateStamp + timeStamp);
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) <= 2) {
        	cleanOldLogs();
        }
    }
 
    public String getLog(int id){

        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = 
                db.query(TABLE_LOG, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
 
        if (cursor != null)
            cursor.moveToFirst();
 
        String log= cursor.getString(2);

        db.close();
        
        //Log.d("getLog("+id+")", log);
 
        return log;
    }

    // Get Logs in a day
    public List<String> getLogsByDate(String date) {
        List<String> logs = new LinkedList<String>();
 
        String query = "SELECT  * FROM " + TABLE_LOG + " WHERE date = " + date + " ORDER BY msg DESC";
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        if (cursor.moveToFirst()) {
            do {
                String log = cursor.getString(2);
                logs.add(log);
            } while (cursor.moveToNext());
        }

        db.close();
        
        //Log.d("getLogsByDate()" + date, logs.toString());
 
        return logs;
    }
    
    // Get All Logs
    public List<String> getAllLogs() {
        List<String> logs = new LinkedList<String>();
 
        String query = "SELECT  * FROM " + TABLE_LOG;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        if (cursor.moveToFirst()) {
            do {
                String log = cursor.getString(1) + '\t' + cursor.getString(2);
                logs.add(log);
            } while (cursor.moveToNext());
        }

        db.close();
        
        //Log.d("getAllLogs()", logs.toString());
 
        return logs;
    }
    
    public Boolean cleanOldLogs() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1);
    	String dateStamp = new SimpleDateFormat("yyyyMM00", java.util.Locale.getDefault()).format(cal.getTime());
        
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_LOG, DATE+" < ?", new String[] { dateStamp });
        db.close();
        Log.d("cleanOldLogs", String.valueOf(count) + " before " + dateStamp);
        
        return true;
    }
}