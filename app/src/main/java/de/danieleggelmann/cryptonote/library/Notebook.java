package de.danieleggelmann.cryptonote.library;

import java.util.ArrayList;

public class Notebook {
	
	protected final static String ROOT_NAME = "root";
	
	protected BlockFile mBlockFile;
	protected NotebookDirectory mRoot;
	
	public Notebook(BlockFile blockFile) throws OperationFailedException {
		mBlockFile = blockFile;
		
		if(mBlockFile.GetBlockCount() == 0)
			throw new OperationFailedException();
		
		int rootBlockId = mBlockFile.GetId(0);
		
		mRoot = new NotebookDirectory(this, rootBlockId, ROOT_NAME);
	}
	
	public Notebook(BlockFile blockFile, int rootBlockId, ArrayList<NotebookElement> rootElements) {
		mBlockFile = blockFile;
		mRoot = new NotebookDirectory(this, rootBlockId, ROOT_NAME, rootElements);
	}
	
	public byte[] ReadBlock(int blockId) throws OperationFailedException {
		try {
			return mBlockFile.ReadBlock(blockId);
		} catch (InvalidBlockIdException | InvalidBlockException e) {
			throw new OperationFailedException();
		}
	}
	
	public void WriteBlock(int blockId, byte[] data) throws OperationFailedException {
		try {
			mBlockFile.ReplaceBlock(blockId, data);
		} catch (InvalidBlockIdException | InvalidBlockException e) {
			throw new OperationFailedException();
		}
	}
	
	public void RemoveBlock(int blockId) throws OperationFailedException {
		try {
			mBlockFile.RemoveBlock(blockId);
		} catch (InvalidBlockIdException | InvalidBlockException e) {
			throw new OperationFailedException();
		}
	}
	
	public int AddBlock(byte[] data) throws OperationFailedException {
		try {
			return mBlockFile.NewBlock(data);
		} catch (InvalidBlockException e) {
			throw new OperationFailedException();
		}
	}
	
	public NotebookDirectory CreateDirectory(String name) throws OperationFailedException {
		int blockId = AddBlock(new byte[0]);
		
		return new NotebookDirectory(this, blockId, name, new ArrayList<NotebookElement>());
	}
	
	public NotebookNote CreateNote(String name) throws OperationFailedException {
		byte[] data = new byte[0];
		
		int blockId = AddBlock(data);
		
		return new NotebookNote(this, blockId, name, data);
	}
	
	public void Save() {
		mBlockFile.Save();
	}
	
	public NotebookDirectory GetRoot() {
		return mRoot;
	}
	
	public static Notebook Create(BlockFile blockFile) throws OperationFailedException {
		if(blockFile.GetBlockCount() != 0) // notebooks can only be created in empty block files
			throw new OperationFailedException();
		
		try {
			int blockId = blockFile.NewBlock(new byte[0]);
			
			return new Notebook(blockFile, blockId, new ArrayList<NotebookElement>());
		} catch (InvalidBlockException e) {
			throw new OperationFailedException();
		}
	}

}
