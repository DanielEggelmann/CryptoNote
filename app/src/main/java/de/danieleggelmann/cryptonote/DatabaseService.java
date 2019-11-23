package de.danieleggelmann.cryptonote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class DatabaseService extends Service {

    private static final String DATABASE_FILE = "main";

    private final IBinder binder = new DatabaseBinder();

    private Database mDatabase;

    public class DatabaseBinder extends Binder {
        DatabaseService getService()
        {
            return DatabaseService.this;
        }
    }

    public DatabaseService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void Login(String password)
    {
        try {
            mDatabase = Database.Open(DATABASE_FILE, password, this);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Notebook GetNotebook()
    {
        return mDatabase.GetNotebook();
    }

}
