package com.elvizlai.h9.test;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.elvizlai.h9.config.ApplictionHandler;

/**
 * Created by huagai on 14-4-22.
 */
public class CreateContactsDB extends SQLiteOpenHelper {
    public CreateContactsDB(){
        super(ApplictionHandler.getContext(),"hg.contacts.db",null,1);
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
