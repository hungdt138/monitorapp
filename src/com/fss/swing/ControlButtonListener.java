package com.fss.swing;

import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface ControlButtonListener
{
	int ACTION_NONE = 0;
	int ACTION_ADD = 1;
	int ACTION_ADD_COPY = 9;
	int ACTION_MODIFY = 2;
	int ACTION_REMOVE = 3;
	int ACTION_SEARCH = 4;
	int ACTION_PRINT = 5;
	int ACTION_SAVE = 6;
	int ACTION_CANCEL = 7;
	int ACTION_EXIT = 8;

	boolean add();
	boolean modify();
	boolean remove();
	boolean search();
	boolean print();
	boolean validateInput(int iOldAction,int iNewAction);
	void onChangeAction(int iOldAction,int iNewAction);
	void exit();
	void backup();
	void restore();
	void clearBackup();
	boolean isRestorable();
	//boolean isChanged();

	// Return the module name associated with control button
	String getPermission();
	JRootPane getRootPane();
}
