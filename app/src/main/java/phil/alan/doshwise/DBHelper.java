package phil.alan.doshwise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.math.BigDecimal;

import java.util.Date;

/**
 * Created by Alan on 03/05/2017.
 */

// drop peoplexpenses, drop expenses, drop people, drop households

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "doshwise.db";

    public static final String TABLE_HOUSEHOLDS = "households";
    public static final String COL_H1 = "hid";
    public static final String COL_H2 = "name";
    public static final String COL_H3 = "numPeople";

    public static final String TABLE_PEOPLE = "people";
    public static final String COL_P1 = "pid";
    public static final String COL_P2 = "hid";
    public static final String COL_P3 = "name";

    public static final String TABLE_EXPENSES = "expenses";
    public static final String COL_E1 = "eid";
    public static final String COL_E2 = "name";
    public static final String COL_E3 = "amount";
    public static final String COL_E4 = "month"; //cant wait to finish college ,

    public static final String TABLE_PEOPLEXPENSES = "peoplexpenses";
    public static final String COL_PE1 = "pid";
    public static final String COL_PE2 = "eid";

    /*
    Constructor
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /*
    Method To Create Tables - db problem so, the tables?
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HOUSEHOLDS + " (hid INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, numPeople INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_PEOPLE + " (pid INTEGER PRIMARY KEY AUTOINCREMENT, hid INTEGER NOT NULL, name TEXT NOT NULL, FOREIGN KEY (hid) REFERENCES households (hid))");
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " (eid INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, amount DECIMAL(9,2) NOT NULL, month TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_PEOPLEXPENSES + " (pid INTEGER, eid INTEGER, PRIMARY KEY (pid, eid), FOREIGN KEY (pid) REFERENCES people (pid), FOREIGN KEY (eid) REFERENCES expenses (eid))");
    }

    /*
    Method To Update Database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLEXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSEHOLDS);
        onCreate(db);
    }

    /*
    Methods To Insert Data
     */
    // Household Data
    public boolean insertHouseholdData(String name, String numPeople) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_H2,name);
        values.put(COL_H3,numPeople);

        long result = db.insert(TABLE_HOUSEHOLDS, null, values);
        if(result == -1) {
            return false;
        }
        else
            return true;
    }

    // People Data
    public boolean insertPeopleData(Integer hid, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_P2,hid);
        values.put(COL_P3,name);

        long result = db.insert(TABLE_PEOPLE, null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    // Expenses Data
    public boolean insertExpensesData(String name, String amount, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_E2,name);
        values.put(COL_E3,amount);
        values.put(COL_E4,month);

        long result = db.insert(TABLE_EXPENSES, null, values); // app froze
        if(result == -1)
            return false;
        else
            return true;
    }

    // Peoplexpenses Data enter data
    public boolean insertPEData(Integer pid, Integer eid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PE1,pid);
        values.put(COL_PE2,eid);

        long result = db.insert(TABLE_PEOPLEXPENSES, null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    /*
    Methods To Retrieve Data
     */
    // Household Data
    public Cursor getAllHouseholdData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOUSEHOLDS, null);
    }

    // People Data
    public Cursor getAllPeopleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PEOPLE, null);
    }
    // People Names
    public Cursor getPeopleNames() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT name FROM " + TABLE_PEOPLE, null);
    }

    // Expenses Data
    public Cursor getAllExpensesData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT eid, name, amount FROM EXPENSES", null);
    }
    public Cursor getExpenseHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT month, SUM(amount) FROM " + TABLE_EXPENSES + " GROUP BY month", null);
    }
    public Cursor splitExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT EXPENSES.eid, PEOPLEXPENSES.pid, PEOPLE.name, SUM(amount.amount) FROM EXPENSES INNER JOIN PEOPLEXPENSES ON PEOPLEXPENSES.eid = EXPENSES.eid INNER JOIN (SELECT query1.eid, query1.amount\nFROM (SELECT EXPENSES.eid, SUM(EXPENSES.amount) / PEOPLEXPENSES.amountOfPPL AS amount FROM HOUSEHOLDS INNER JOIN PEOPLE ON PEOPLE.hid = HOUSEHOLDS.hid INNER JOIN ( SELECT eid, MAX(pid) AS pid, COUNT(*) AS amountOfPPL FROM PEOPLEXPENSES GROUP BY eid ) AS PEOPLEXPENSES ON PEOPLEXPENSES.pid = PEOPLE.pid INNER JOIN EXPENSES ON EXPENSES.eid = PEOPLEXPENSES.eid WHERE HOUSEHOLDS.hid = 1 GROUP BY EXPENSES.eid) AS query1) AS amount ON amount.eid = EXPENSES.eid INNER JOIN PEOPLE ON PEOPLEXPENSES.pid = PEOPLE.pid GROUP BY PEOPLEXPENSES.pid", null);
    }
    public Cursor numTimes() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT eid, COUNT(eid) FROM " + TABLE_PEOPLEXPENSES + " GROUP BY eid" ,null);
    }

    /*
    Methods To Update Data
     */
    // Household Data
    public boolean updateHouseHoldData(String id, String name, Integer numPeople) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_H1, id);
        values.put(COL_H2, name);
        values.put(COL_H3, numPeople);

        db.update(TABLE_HOUSEHOLDS, values, "ID = ?", new String[] { id });
        return true;
    }

    /*
    Methods To Delete Data
     */
    public Integer deleteExpenseData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXPENSES, "eid = ?", new String[] { id });
    }
    public Integer deletePeopleExpenses (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PEOPLEXPENSES, "eid = ?", new String[] { id });
    }

    /*
    Methods to retrieve max ID
     */
    // Max Household ID
    public Cursor maxHouseholdID () {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT MAX(hid) AS hid FROM households", null);
    }

    // Max Expense ID
    public Cursor maxExpenseID () {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT MAX(eid) AS eid FROM expenses", null);
    }
}