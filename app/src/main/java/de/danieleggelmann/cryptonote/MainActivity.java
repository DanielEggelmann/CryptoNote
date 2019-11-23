package de.danieleggelmann.cryptonote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button button_login;
    protected EditText edit_password;

    protected DatabaseService mDatabaseService;
    protected boolean mDatabaseServiceConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(this);

        edit_password = (EditText) findViewById(R.id.edit_password);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent databaseIntent = new Intent(this, DatabaseService.class);
        bindService(databaseIntent, mDatabaseConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        unbindService(mDatabaseConnection);
        mDatabaseServiceConnected = false;
    }

    @Override
    public void onClick(View v) {
        if(v == button_login)
        {
            if(mDatabaseServiceConnected)
            {
                mDatabaseService.Login(edit_password.getText().toString());

                for(int i = 0; i < 3; i++)
                {
                    Note note = mDatabaseService.GetNotebook().NewNote();
                    note.SetTitle(String.valueOf(i));
                    note.SetContent("");
                    note.Save();
                }

                mDatabaseService.GetNotebook().Save();

                Intent notebookIntent = new Intent(this, NotebookActivity.class);
                startActivity(notebookIntent);
            }
        }
    }

    private ServiceConnection mDatabaseConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.DatabaseBinder binder = (DatabaseService.DatabaseBinder) service;
            mDatabaseService = binder.getService();
            mDatabaseServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDatabaseServiceConnected = false;
        }
    };
}
