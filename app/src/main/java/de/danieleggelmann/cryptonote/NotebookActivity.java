package de.danieleggelmann.cryptonote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.danieleggelmann.cryptonote.library.Notebook;
import de.danieleggelmann.cryptonote.library.NotebookDirectory;
import de.danieleggelmann.cryptonote.library.NotebookNote;
import de.danieleggelmann.cryptonote.library.OperationFailedException;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotebookActivity extends AppCompatActivity implements NewNotebookElementDialogFragment.NewNotebookElementDialogListener {

    protected DatabaseService mDatabaseService;
    protected boolean mDatabaseServiceConnected;

    protected NotebookDirectory mDirectory;

    protected RecyclerView recycler_notebooks;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    FloatingActionButton button_new_notebookelement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            mDirectory = mDatabaseService.GetRoot();

            setContentView(R.layout.activity_notebook);

            recycler_notebooks = (RecyclerView) findViewById(R.id.recycler_notebook);

            recycler_notebooks.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(NotebookActivity.this);
            recycler_notebooks.setLayoutManager(mLayoutManager);

            mAdapter = new NotebookRecyclerAdapter(mDirectory);
            recycler_notebooks.setAdapter(mAdapter);

            button_new_notebookelement = findViewById(R.id.button_new_notebookelement);
            button_new_notebookelement.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    DialogFragment newNotebookElementDialog = new NewNotebookElementDialogFragment();
                    newNotebookElementDialog.show(getFragmentManager(), "new_notebookelement");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDatabaseServiceConnected = false;
            mDirectory = null;
        }
    };

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String name, int type) {
        if(mDatabaseServiceConnected) {
            if(type + 1 == NotebookNote.TYPE) {
                NotebookNote note = mDatabaseService.CreateNote(name);
                try {
                    note.Save();
                    mDirectory.AddElement(note);
                    mDirectory.Save();
                    mDatabaseService.Save();
                } catch (OperationFailedException e) {
                    e.printStackTrace();
                }
            }
            else {
                NotebookDirectory directory = mDatabaseService.CreateDirectory(name);
                try {
                    directory.Save();
                    mDirectory.AddElement(directory);
                    mDirectory.Save();
                    mDatabaseService.Save();
                } catch (OperationFailedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
