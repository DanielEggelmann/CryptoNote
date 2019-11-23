package de.danieleggelmann.cryptonote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;

import de.danieleggelmann.cryptonote.library.EncryptedBlockFile;
import de.danieleggelmann.cryptonote.library.InvalidBlockFileException;
import de.danieleggelmann.cryptonote.library.MemoryFile;
import de.danieleggelmann.cryptonote.library.Notebook;
import de.danieleggelmann.cryptonote.library.NotebookDirectory;
import de.danieleggelmann.cryptonote.library.NotebookNote;
import de.danieleggelmann.cryptonote.library.OperationFailedException;

public class DatabaseService extends Service {

    private final static String NOTEBOOK_FILE = "notebook";

    private final IBinder binder = new DatabaseBinder();

    private Notebook mNotebook;

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
        File file = new File(this.getDataDir(), NOTEBOOK_FILE);

        if(file.exists()) {
            try {
                mNotebook = new Notebook(new EncryptedBlockFile(new MemoryFile(file), password));
            } catch (OperationFailedException | InvalidBlockFileException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                mNotebook = Notebook.Create(EncryptedBlockFile.Create(new MemoryFile(file), password));
            } catch (OperationFailedException | InvalidBlockFileException e) {
                e.printStackTrace();
            }
        }
    }

    public NotebookDirectory GetRoot() {
        if(mNotebook != null) {
            return mNotebook.GetRoot();
        }

        return null;
    }

    public NotebookNote CreateNote(String name) {
        if(mNotebook != null) {
            try {
                NotebookNote note = mNotebook.CreateNote(name);

                return note;
            } catch (OperationFailedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public NotebookDirectory CreateDirectory(String name) {
        if(mNotebook != null) {
            try {
                NotebookDirectory directory = mNotebook.CreateDirectory(name);

                return directory;
            } catch (OperationFailedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void Save() {
        if(mNotebook != null) {
            mNotebook.Save();
        }
    }

}
