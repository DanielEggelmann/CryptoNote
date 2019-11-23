package de.danieleggelmann.cryptonote;

import java.util.ArrayList;
import java.util.Collections;

public class Notebook extends DatabaseElement {

    public static final char TYPE = 'B';

    private static final String SPLIT_SEQUENCE = "\n";

    private String mName;
    private ArrayList<DatabaseElement> mContent;

    public Notebook(String id, Database database) {
        super(id, database);
    }

    public Notebook(String id, Database database, String name, DatabaseElement[] content) {
        super(id, database);

        mName = name;
        mContent = new ArrayList<>();
        Collections.addAll(mContent, content);
    }

    public void Load()
    {
        String data = new String(mDatabase.DecryptFile(mId));
        String[] elements = data.split(SPLIT_SEQUENCE);

        mName = elements[0];

        mContent = new ArrayList<>();
        for(int i = 1; i < elements.length; i++)
        {
            if(elements[i].charAt(0) == TYPE)
            {
                mContent.add(new Notebook(elements[i].substring(1), mDatabase));
            }
            else
            {
                mContent.add(new Note(elements[i].substring(1), mDatabase));
            }
        }
    }

    public void Save()
    {
        String data = mName;
        for(DatabaseElement element : mContent)
        {
            if(element.IsNotebook())
            {
                Notebook notebook = element.ToNotebook();
                data += SPLIT_SEQUENCE + notebook.GetType() + notebook.GetId();
            }
            else
            {
                Note note = element.ToNote();
                data += SPLIT_SEQUENCE + note.GetType() + note.GetId();
            }
        }
        mDatabase.EncryptFile(mId, data.getBytes());
    }

    public Note NewNote()
    {
        if(mContent == null)
        {
            Load();
        }

        String id = mDatabase.GetNewId();
        Note note = new Note(id, mDatabase);
        mContent.add(note);

        return note;
    }

    public Notebook NewNotebook(String name)
    {
        if(mContent == null)
        {
            Load();
        }

        String id = mDatabase.GetNewId();
        Notebook notebook = new Notebook(id, mDatabase, name, new DatabaseElement[0]);
        mContent.add(notebook);

        return notebook;
    }

    public String GetName()
    {
        if(mName == null)
        {
            Load();
        }

        return mName;
    }

    public void SetName(String name)
    {
        mName = name;
    }

    public DatabaseElement[] GetContent()
    {
        if(mContent == null)
        {
            Load();
        }

        DatabaseElement[] content = new DatabaseElement[mContent.size()];
        mContent.toArray(content);

        return content;
    }

    @Override
    public char GetType()
    {
        return TYPE;
    }

}
