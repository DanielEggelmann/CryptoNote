package de.danieleggelmann.cryptonote;

import java.io.Serializable;

public class DatabaseElement {

    public static final char TYPE = 'D';

    protected Database mDatabase;
    protected String mId;

    public DatabaseElement(String id, Database database)
    {
        mId = id;
        mDatabase = database;
    }

    public String GetId()
    {
        return mId;
    }

    public boolean IsNotebook()
    {
        return GetType() == Notebook.TYPE;
    }

    public boolean IsNote()
    {
        return GetType() == Note.TYPE;
    }

    public Notebook ToNotebook()
    {
        return (Notebook) this;
    }

    public Note ToNote()
    {
        return (Note) this;
    }

    public char GetType()
    {
        return TYPE;
    }
}
