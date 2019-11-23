package de.danieleggelmann.cryptonote.library;

public class NotebookNote extends NotebookElement {
	
	public static final short TYPE = 2;
	
	protected byte[] mData;

	public NotebookNote(Notebook notebook, int blockId, String name) {
		super(notebook, blockId, name);
	}
	
	public NotebookNote(Notebook notebook, int blockId, String name, byte[] data) {
		super(notebook, blockId, name);
		
		mData = data;
	}
	
	protected void _LazyLoad() throws OperationFailedException {
		if(mData == null) {
			mData = mNotebook.ReadBlock(mBlockId);
		}
	}
	
	public byte[] GetData() throws OperationFailedException {
		_LazyLoad();
		
		return mData;
	}
	
	public void SetData(byte[] data) throws OperationFailedException {
		_LazyLoad();
		
		mData = data;
	}
	
	@Override
	public void Save() throws OperationFailedException {
		_LazyLoad();
		
		mNotebook.WriteBlock(mBlockId, mData);
	}

	@Override
	public short GetType() {
		return TYPE;
	}


}
