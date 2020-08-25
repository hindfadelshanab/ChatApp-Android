package com.ogsoft.scobimessenger.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ogsoft.scobimessenger.models.User;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "scobiMessenger";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_USERS = "users";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_UUID = "uuid";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_USERNAME = "username";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_CREATED_AT = "createdAt";
    private static final String KEY_USER_UPDATED_AT = "updatedAt";

    private static ChatDatabaseHelper sInstance;

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    private ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS +
                "(" +
                    KEY_USER_ID + " INTEGER PRIMARY KEY," +
                    KEY_USER_UUID + " TEXT," +
                    KEY_USER_NAME + " TEXT," +
                    KEY_USER_USERNAME + " TEXT," +
                    KEY_USER_EMAIL + " TEXT," +
                    KEY_USER_CREATED_AT + " TEXT," +
                    KEY_USER_UPDATED_AT + " TEXT" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    // Insert a post into the database
    public void addUser(User user) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_UUID, user.uuid);
            values.put(KEY_USER_NAME, user.name); // check null pointer exception
            values.put(KEY_USER_USERNAME, user.username);
            values.put(KEY_USER_EMAIL, user.email); // check null pointer exception
            values.put(KEY_USER_CREATED_AT, user.createdAt);
            values.put(KEY_USER_UPDATED_AT, user.updatedAt);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public void addOrUpdateUser(User user) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_UUID, user.uuid);
            values.put(KEY_USER_NAME, user.name); // check null pointer exception
            values.put(KEY_USER_USERNAME, user.username);
            values.put(KEY_USER_EMAIL, user.email); // check null pointer exception
            values.put(KEY_USER_CREATED_AT, user.createdAt);
            values.put(KEY_USER_UPDATED_AT, user.updatedAt);

            // First try to update the user in case the user already exists in the database
            // This assumes usernames are unique
            int rows = db.update(TABLE_USERS, values, KEY_USER_USERNAME + "= ?", new String[]{user.username});

            // Check if update not succeeded
            if (rows != 1) {
                // user with this userName did not already exist, so insert new user
                db.insertOrThrow(TABLE_USERS, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public static synchronized  ChatDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ChatDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
}