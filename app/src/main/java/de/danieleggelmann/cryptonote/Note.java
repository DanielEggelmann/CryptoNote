package de.danieleggelmann.cryptonote;

public class Note extends DatabaseElement {

    public static final char TYPE = 'N';

    protected static final String SPLIT_SEQUENCE = "\n";

    protected String mTitle;
    protected String mContent;

    public Note(String id, Database database)
    {
        super(id, database);
    }

    protected void Load()
    {
        String data = new String(mDatabase.DecryptFile(mId));
        int splitPos = data.indexOf(SPLIT_SEQUENCE);
        mTitle = data.substring(0, splitPos - 1);
        mContent = data.substring(splitPos + 1);
    }

    public void Save()
    {
        String data = mTitle + SPLIT_SEQUENCE + mContent;
        mDatabase.EncryptFile(mId, data.getBytes());
    }

    public String GetContent()
    {
        if(mContent == null)
        {
            Load();
        }

        return mContent;
    }

    public String GetTitle()
    {
        if(mTitle == null)
        {
            Load();
        }

        return mTitle;
    }

    public void SetContent(String content)
    {
        mContent = content;
    }

    public void SetTitle(String title)
    {
        mTitle = title;
    }

    @Override
    public char GetType()
    {
        return TYPE;
    }
}
