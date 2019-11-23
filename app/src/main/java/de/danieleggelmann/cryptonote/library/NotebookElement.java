package de.danieleggelmann.cryptonote.library;

public abstract class NotebookElement {
	
	protected Notebook mNotebook;
	protected int mBlockId;
	protected String mName;
	
	public NotebookElement(Notebook notebook, int blockId, String name) {
		mNotebook = notebook;
		mBlockId = blockId;
		mName = name;
	}
	
	public int GetBlockId() {
		return mBlockId;
	}
	
	public String GetName() {
		return mName;
	}
	
	public void Save() throws OperationFailedException {
		throw new NoSuchMethodError();
	}
	
	public void Remove() throws OperationFailedException {
		mNotebook.RemoveBlock(mBlockId);
	}
	
	public short GetType() {
		throw new NoSuchMethodError();
	}

}
