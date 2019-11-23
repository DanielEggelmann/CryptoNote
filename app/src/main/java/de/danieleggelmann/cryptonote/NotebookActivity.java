package de.danieleggelmann.cryptonote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.LinearLayout;

public class NotebookActivity extends AppCompatActivity {

    protected DatabaseService mDatabaseService;
    protected boolean mDatabaseServiceConnected;
    protected Notebook mNotebook;

    protected RecyclerView recycler_notebooks;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
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
    }

    private ServiceConnection mDatabaseConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.DatabaseBinder binder = (DatabaseService.DatabaseBinder) service;
            mDatabaseService = binder.getService();
            mDatabaseServiceConnected = true;
            mNotebook = mDatabaseService.GetNotebook();

            recycler_notebooks = (RecyclerView) findViewById(R.id.recycler_notebook);

            recycler_notebooks.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(NotebookActivity.this);
            recycler_notebooks.setLayoutManager(mLayoutManager);

            mAdapter = new NotebookRecyclerAdapter(mNotebook);
            recycler_notebooks.setAdapter(mAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDatabaseServiceConnected = false;
            mNotebook = null;
        }
    };
}
