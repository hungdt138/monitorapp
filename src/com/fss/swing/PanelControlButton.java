package com.fss.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.util.*;
import com.fss.dictionary.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class PanelControlButton extends JPanel implements LanguageUpdatable
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static int PROCESS_DIRECT = 0;
	public static int PROCESS_DIRECT_WITH_WARNING = 1;
	public static int PROCESS_INDIRECT = 2;
	public static int PROCESS_INDIRECT_WITH_WARNING = 3;
	////////////////////////////////////////////////////////
	public JButton btnAdd = new JButton();
	public JButton btnAddCopy = new JButton();
	public JButton btnModify = new JButton();
	public JButton btnRemove = new JButton();
	public JButton btnSearch = new JButton();
	public JButton btnPrint = new JButton();
	public JButton btnSave = new JButton();
	public JButton btnCancel = new JButton();
	public JButton btnExit = new JButton();
	private int miAddProcessType = PROCESS_INDIRECT;
	private int miAddCopyProcessType = PROCESS_INDIRECT;
	private int miModifyProcessType = PROCESS_INDIRECT;
	private int miRemoveProcessType = PROCESS_DIRECT_WITH_WARNING;
	private int miSearchProcessType = PROCESS_INDIRECT;
	private int miPrintProcessType = PROCESS_DIRECT;
	private int miAction = 0;
	private int miLastAction = 0;
	private ControlButtonListener mlistener = null;
	private String mstrAddWarning = null;
	private String mstrAddCopyWarning = null;
	private String mstrModifyWarning = null;
	private String mstrRemoveWarning = null;
	private String mstrSearchWarning = null;
	private String mstrPrintWarning = null;
	private boolean mbAutoLayoutAdd = true;
	private boolean mbAutoLayoutAddCopy = true;
	private boolean mbAutoLayoutModify = true;
	private boolean mbAutoLayoutRemove = true;
	private boolean mbAutoLayoutSearch = true;
	private boolean mbAutoLayoutPrint = true;
	private boolean mbAutoLayoutExit = true;
	private boolean mbAllowAdd = true;
	private boolean mbAllowAddCopy = true;
	private boolean mbAllowModify = true;
	private boolean mbAllowRemove = true;
	private boolean mbAllowSearch = true;
	private boolean mbAllowPrint = false;
	private boolean mbCheckSearchPermission = true;
	private boolean mbCheckAddPermission = true;
	private boolean mbCheckModifyPermission = true;
	private boolean mbCheckRemovePermission = true;
	private boolean mbCountinuousAdding = true;
	private boolean mbCountinuousAddingCopy = true;
	private boolean mbInitialized = false;
	////////////////////////////////////////////////////////
	public PanelControlButton()
	{
	}
	////////////////////////////////////////////////////////
	public PanelControlButton(ControlButtonListener listener)
	{
		init(listener);
	}
	////////////////////////////////////////////////////////
	public void init(ControlButtonListener listener)
	{
		////////////////////////////////////////////////////////
		setControlButtonListener(listener);
		setLayout(new GridLayout(1,5,4,4));
		btnAdd.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnAddCopy.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnModify.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnRemove.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnSearch.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnPrint.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnSave.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnCancel.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		btnExit.setPreferredSize(Skin.BUTTON_MIN_SIZE);
		add(btnAdd);
		add(btnAddCopy);
		add(btnModify);
		add(btnRemove);
		add(btnSearch);
		add(btnPrint);
		add(btnSave);
		add(btnCancel);
		add(btnExit);
		////////////////////////////////////////////////////////
		checkPermission();
		updateLanguage();
		mbInitialized = true;
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		btnAdd.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						add();
					}
				});
			}
		});
		btnAddCopy.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						addCopy();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnModify.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						modify();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnRemove.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						remove();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnSearch.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						search();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnPrint.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						print();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnSave.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						if(save())
						{
							if(miLastAction == ControlButtonListener.ACTION_ADD)
								completeAdd();
							else if(miLastAction == ControlButtonListener.ACTION_ADD_COPY)
								completeAddCopy();
						}
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnCancel.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						cancel();
					}
				});
			}
		});
		////////////////////////////////////////////////////////
		btnExit.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						if(exit())
							mlistener.exit();
					}
				});
			}
		});
	}
	////////////////////////////////////////////////////////
	public boolean isInitialized()
	{
		return mbInitialized;
	}
	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		DefaultDictionary.applyButton(btnAdd,"Add");
		DefaultDictionary.applyButton(btnAddCopy,"AddCopy");
		DefaultDictionary.applyButton(btnModify,"Modify");
		DefaultDictionary.applyButton(btnRemove,"Remove");
		DefaultDictionary.applyButton(btnSearch,"Search");
		DefaultDictionary.applyButton(btnPrint,"Print");
		DefaultDictionary.applyButton(btnSave,"OK");
		DefaultDictionary.applyButton(btnCancel,"Cancel");
		DefaultDictionary.applyButton(btnExit,"Exit");
		mstrAddWarning = DefaultDictionary.getString("PanelControlButton.AddWarning");
		mstrAddCopyWarning = DefaultDictionary.getString("PanelControlButton.AddCopyWarning");
		mstrModifyWarning = DefaultDictionary.getString("PanelControlButton.ModifyWarning");
		mstrRemoveWarning = DefaultDictionary.getString("PanelControlButton.RemoveWarning");
		mstrSearchWarning = DefaultDictionary.getString("PanelControlButton.SearchWarning");
		mstrPrintWarning = DefaultDictionary.getString("PanelControlButton.PrintWarning");
	}
	////////////////////////////////////////////////////////
	public void checkPermission()
	{
		String strModuleRight = mlistener.getPermission();
		if(mbCheckSearchPermission && strModuleRight.indexOf("S") < 0)
			btnSearch.setEnabled(false);
		else
			btnSearch.setEnabled(true);

		if(mbCheckAddPermission && strModuleRight.indexOf("I") < 0)
		{
			btnAdd.setEnabled(false);
			btnAddCopy.setEnabled(false);
		}
		else
		{
			btnAdd.setEnabled(true);
			btnAddCopy.setEnabled(true);
		}

		if(mbCheckModifyPermission && strModuleRight.indexOf("U") < 0)
			btnModify.setEnabled(false);
		else
			btnModify.setEnabled(true);

		if(mbCheckRemovePermission && strModuleRight.indexOf("D") < 0)
			btnRemove.setEnabled(false);
		else
			btnRemove.setEnabled(true);
	}
	////////////////////////////////////////////////////////
	public String getAddWarningMessage()
	{
		return mstrAddWarning;
	}
	public void setAddWarningMessage(String strMessage)
	{
		mstrAddWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public String getAddCopyWarningMessage()
	{
		return mstrAddCopyWarning;
	}
	public void setAddCopyWarningMessage(String strMessage)
	{
		mstrAddCopyWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public String getModifyWarningMessage()
	{
		return mstrModifyWarning;
	}
	public void setModifyWarningMessage(String strMessage)
	{
		mstrModifyWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public String getRemoveWarningMessage()
	{
		return mstrRemoveWarning;
	}
	public void setRemoveWarningMessage(String strMessage)
	{
		mstrRemoveWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public String getSearchWarningMessage()
	{
		return mstrSearchWarning;
	}
	public void setSearchWarningMessage(String strMessage)
	{
		mstrSearchWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public String getPrintWarningMessage()
	{
		return mstrPrintWarning;
	}
	public void setPrintWarningMessage(String strMessage)
	{
		mstrPrintWarning = strMessage;
	}
	////////////////////////////////////////////////////////
	public int getAddProcessType()
	{
		return miAddProcessType;
	}
	////////////////////////////////////////////////////////
	public void setAddProcessType(int iAddProcessType)
	{
		miAddProcessType = iAddProcessType;
	}
	////////////////////////////////////////////////////////
	public int getAddCopyProcessType()
	{
		return miAddCopyProcessType;
	}
	////////////////////////////////////////////////////////
	public void setAddCopyProcessType(int iAddCopyProcessType)
	{
		miAddCopyProcessType = iAddCopyProcessType;
	}
	////////////////////////////////////////////////////////
	public boolean isCountinuousAdding()
	{
		return mbCountinuousAdding;
	}
	////////////////////////////////////////////////////////
	public void setCountinuousAdding(boolean bCountinuousAdding)
	{
		mbCountinuousAdding = bCountinuousAdding;
	}
	////////////////////////////////////////////////////////
	public boolean isCountinuousAddingCopy()
	{
		return mbCountinuousAddingCopy;
	}
	////////////////////////////////////////////////////////
	public void setCountinuousAddingCopy(boolean bCountinuousAddingCopy)
	{
		mbCountinuousAddingCopy = bCountinuousAddingCopy;
	}
	////////////////////////////////////////////////////////
	public boolean isAutoLayout(int iButton)
	{
		if(iButton == ControlButtonListener.ACTION_ADD)
			return mbAllowAdd;
		else if(iButton == ControlButtonListener.ACTION_ADD_COPY)
			return mbAllowAddCopy;
		else if(iButton == ControlButtonListener.ACTION_MODIFY)
			return mbAllowModify;
		else if(iButton == ControlButtonListener.ACTION_REMOVE)
			return mbAllowRemove;
		else if(iButton == ControlButtonListener.ACTION_SEARCH)
			return mbAllowSearch;
		else if(iButton == ControlButtonListener.ACTION_PRINT)
			return mbAllowPrint;
		else if(iButton == ControlButtonListener.ACTION_SAVE)
			return true;
		else if(iButton == ControlButtonListener.ACTION_CANCEL)
			return true;
		return false;
	}
	////////////////////////////////////////////////////////
	public void setAutoLayout(boolean bAutoLayout,int iButton)
	{
		if(iButton == ControlButtonListener.ACTION_ADD)
			mbAutoLayoutAdd = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_ADD_COPY)
			mbAutoLayoutAddCopy = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_MODIFY)
			mbAutoLayoutModify = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_REMOVE)
			mbAutoLayoutRemove = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_SEARCH)
			mbAutoLayoutSearch = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_PRINT)
			mbAutoLayoutPrint = bAutoLayout;
		else if(iButton == ControlButtonListener.ACTION_EXIT)
			mbAutoLayoutExit = bAutoLayout;
	}
	////////////////////////////////////////////////////////
	public boolean allowAdd()
	{
		return mbAllowAdd;
	}
	////////////////////////////////////////////////////////
	public void setAllowAdd(boolean bAllowAdd)
	{
		mbAllowAdd = bAllowAdd;
		if(!mbAllowAdd)
		{
			this.remove(btnAdd);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public boolean allowAddCopy()
	{
		return mbAllowAddCopy;
	}
	////////////////////////////////////////////////////////
	public void setAllowAddCopy(boolean bAllowAddCopy)
	{
		mbAllowAddCopy = bAllowAddCopy;
		if(!mbAllowAddCopy)
		{
			this.remove(btnAddCopy);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public int getModifyProcessType()
	{
		return miModifyProcessType;
	}
	////////////////////////////////////////////////////////
	public void setModifyProcessType(int iModifyProcessType)
	{
		miModifyProcessType = iModifyProcessType;
	}
	////////////////////////////////////////////////////////
	public boolean allowModify()
	{
		return mbAllowModify;
	}
	////////////////////////////////////////////////////////
	public void setAllowModify(boolean bAllowModify)
	{
		mbAllowModify = bAllowModify;
		if(!mbAllowModify)
		{
			this.remove(btnModify);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public int getRemoveProcessType()
	{
		return miRemoveProcessType;
	}
	////////////////////////////////////////////////////////
	public void setRemoveProcessType(int iRemoveProcessType)
	{
		miRemoveProcessType = iRemoveProcessType;
	}
	////////////////////////////////////////////////////////
	public void setAllowRemove(boolean bAllowRemove)
	{
		mbAllowRemove = bAllowRemove;
		if(!mbAllowRemove)
		{
			this.remove(btnRemove);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public int getSearchProcessType()
	{
		return miSearchProcessType;
	}
	////////////////////////////////////////////////////////
	public void setSearchProcessType(int iSearchProcessType)
	{
		miSearchProcessType = iSearchProcessType;
	}
	////////////////////////////////////////////////////////
	public boolean allowSearch()
	{
		return mbAllowSearch;
	}
	////////////////////////////////////////////////////////
	public void setAllowSearch(boolean bAllowSearch)
	{
		mbAllowSearch = bAllowSearch;
		if(!mbAllowSearch)
		{
			this.remove(btnSearch);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public int getPrintProcessType()
	{
		return miPrintProcessType;
	}
	////////////////////////////////////////////////////////
	public void setPrintProcessType(int iPrintProcessType)
	{
		miPrintProcessType = iPrintProcessType;
	}
	////////////////////////////////////////////////////////
	public boolean allowPrint()
	{
		return mbAllowPrint;
	}
	////////////////////////////////////////////////////////
	public void setAllowPrint(boolean bAllowPrint)
	{
		mbAllowPrint = bAllowPrint;
		if(!mbAllowPrint)
		{
			this.remove(btnPrint);
			this.updateUI();
		}
	}
	////////////////////////////////////////////////////////
	public boolean isCheckSearchPermissionEnabled()
	{
		return mbCheckSearchPermission;
	}
	////////////////////////////////////////////////////////
	public void setCheckSearchPermissionEnabled(boolean bCheckSearchPermission)
	{
		mbCheckSearchPermission = bCheckSearchPermission;
		checkPermission();
	}
	////////////////////////////////////////////////////////
	public boolean isCheckAddPermissionEnabled()
	{
		return mbCheckAddPermission;
	}
	////////////////////////////////////////////////////////
	public void setCheckAddPermissionEnabled(boolean bCheckAddPermission)
	{
		mbCheckAddPermission = bCheckAddPermission;
		checkPermission();
	}
	////////////////////////////////////////////////////////
	public boolean isCheckModifyPermissionEnabled()
	{
		return mbCheckModifyPermission;
	}
	////////////////////////////////////////////////////////
	public void setCheckModifyPermissionEnabled(boolean bCheckModifyPermission)
	{
		mbCheckModifyPermission = bCheckModifyPermission;
		checkPermission();
	}
	////////////////////////////////////////////////////////
	public boolean isCheckRemovePermissionEnabled()
	{
		return mbCheckRemovePermission;
	}
	////////////////////////////////////////////////////////
	public void setCheckRemovePermissionEnabled(boolean bCheckRemovePermission)
	{
		mbCheckRemovePermission = bCheckRemovePermission;
		checkPermission();
	}
	////////////////////////////////////////////////////////
	public void setActionState()
	{
		this.removeAll();
		this.add(btnSave);
		this.add(btnCancel);
		this.updateUI();
		if(getRootPane() != null)
			getRootPane().setDefaultButton(btnSave);
	}
	////////////////////////////////////////////////////////
	public void setNormalState()
	{
		this.removeAll();
		if(mbAutoLayoutAdd && mbAllowAdd) this.add(btnAdd);
		if(mbAutoLayoutAddCopy && mbAllowAddCopy) this.add(btnAddCopy);
		if(mbAutoLayoutModify && mbAllowModify) this.add(btnModify);
		if(mbAutoLayoutRemove && mbAllowRemove) this.add(btnRemove);
		if(mbAutoLayoutSearch && mbAllowSearch) this.add(btnSearch);
		if(mbAutoLayoutPrint && mbAllowPrint) this.add(btnPrint);
		if(mbAutoLayoutExit) this.add(btnExit);
		this.updateUI();
		if(getRootPane() != null)
			getRootPane().setDefaultButton(btnSearch);
	}
	////////////////////////////////////////////////////////
	public void add()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_ADD))
			return;
		if(miAddProcessType == PROCESS_INDIRECT ||
		   miAddProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_ADD;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miAddProcessType == PROCESS_DIRECT || miAddProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miAddProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getAddWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_ADD,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.add())
			{
				mlistener.onChangeAction(ControlButtonListener.ACTION_ADD,ControlButtonListener.ACTION_SAVE);
				mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
				completeAdd();
			}
			else
			{
				mlistener.onChangeAction(ControlButtonListener.ACTION_ADD,ControlButtonListener.ACTION_CANCEL);
				mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
			}
		}
	}
	////////////////////////////////////////////////////////
	public void addCopy()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_ADD_COPY))
			return;
		if(miAddCopyProcessType == PROCESS_INDIRECT ||
		   miAddCopyProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_ADD_COPY;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miAddCopyProcessType == PROCESS_DIRECT || miAddCopyProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miAddCopyProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getAddCopyWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_ADD_COPY,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.add())
			{
				mlistener.onChangeAction(ControlButtonListener.ACTION_ADD_COPY,ControlButtonListener.ACTION_SAVE);
				mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
				completeAddCopy();
			}
			else
			{
				mlistener.onChangeAction(ControlButtonListener.ACTION_ADD_COPY,ControlButtonListener.ACTION_CANCEL);
				mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
			}
		}
	}
	////////////////////////////////////////////////////////
	public void completeAdd()
	{
		if(isCountinuousAdding())
			btnAdd.doClick();
	}
	////////////////////////////////////////////////////////
	public void completeAddCopy()
	{
		if(isCountinuousAddingCopy())
			btnAddCopy.doClick();
	}
	////////////////////////////////////////////////////////
	public void modify()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_MODIFY))
			return;
		if(miModifyProcessType == PROCESS_INDIRECT ||
		   miModifyProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_MODIFY;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miModifyProcessType == PROCESS_DIRECT || miModifyProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miModifyProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getModifyWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_MODIFY,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.modify())
				mlistener.onChangeAction(ControlButtonListener.ACTION_MODIFY,ControlButtonListener.ACTION_SAVE);
			else
				mlistener.onChangeAction(ControlButtonListener.ACTION_MODIFY,ControlButtonListener.ACTION_CANCEL);
			mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
		}
	}
	////////////////////////////////////////////////////////
	public void remove()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_REMOVE))
			return;
		if(miRemoveProcessType == PROCESS_INDIRECT ||
		   miRemoveProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_REMOVE;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miRemoveProcessType == PROCESS_DIRECT || miRemoveProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miRemoveProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getRemoveWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_REMOVE,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.remove())
				mlistener.onChangeAction(ControlButtonListener.ACTION_REMOVE,ControlButtonListener.ACTION_SAVE);
			else
				mlistener.onChangeAction(ControlButtonListener.ACTION_REMOVE,ControlButtonListener.ACTION_CANCEL);
			mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
		}
	}
	////////////////////////////////////////////////////////
	public void search()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_SEARCH))
			return;
		if(miSearchProcessType == PROCESS_INDIRECT ||
		   miSearchProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_SEARCH;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miSearchProcessType == PROCESS_DIRECT || miSearchProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miSearchProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getSearchWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_SEARCH,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.search())
				mlistener.onChangeAction(ControlButtonListener.ACTION_SEARCH,ControlButtonListener.ACTION_SAVE);
			else
				mlistener.onChangeAction(ControlButtonListener.ACTION_SEARCH,ControlButtonListener.ACTION_CANCEL);
			mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
		}
	}
	////////////////////////////////////////////////////////
	public void print()
	{
		if(!mlistener.validateInput(ControlButtonListener.ACTION_NONE,ControlButtonListener.ACTION_PRINT))
			return;
		if(miPrintProcessType == PROCESS_INDIRECT ||
		   miPrintProcessType == PROCESS_INDIRECT_WITH_WARNING)
		{
			miAction = ControlButtonListener.ACTION_PRINT;
			setActionState();
			mlistener.onChangeAction(ControlButtonListener.ACTION_NONE,miAction);
			mlistener.backup();
		}
		else if(miPrintProcessType == PROCESS_DIRECT || miPrintProcessType == PROCESS_DIRECT_WITH_WARNING)
		{
			if(miPrintProcessType == PROCESS_DIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getPrintWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					mlistener.onChangeAction(ControlButtonListener.ACTION_PRINT,ControlButtonListener.ACTION_CANCEL);
					mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
					return;
				}
			}

			if(mlistener.print())
				mlistener.onChangeAction(ControlButtonListener.ACTION_PRINT,ControlButtonListener.ACTION_SAVE);
			else
				mlistener.onChangeAction(ControlButtonListener.ACTION_PRINT,ControlButtonListener.ACTION_CANCEL);
			mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,ControlButtonListener.ACTION_NONE);
		}
	}
	////////////////////////////////////////////////////////
	public boolean save()
	{
		miLastAction = miAction;
		if(!mlistener.validateInput(miAction,ControlButtonListener.ACTION_SAVE))
			return false;
		if(miAction == ControlButtonListener.ACTION_ADD)
		{
			if(miAddProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getAddWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.add())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		else if(miAction == ControlButtonListener.ACTION_ADD_COPY)
		{
			if(miAddCopyProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getAddCopyWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.add())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		else if(miAction == ControlButtonListener.ACTION_MODIFY)
		{
			if(miModifyProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getModifyWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.modify())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		else if(miAction == ControlButtonListener.ACTION_REMOVE)
		{
			if(miRemoveProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getRemoveWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.remove())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		else if(miAction == ControlButtonListener.ACTION_SEARCH)
		{
			if(miSearchProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getSearchWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.search())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		else if(miAction == ControlButtonListener.ACTION_PRINT)
		{
			if(miPrintProcessType == PROCESS_INDIRECT_WITH_WARNING)
			{
				int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),getPrintWarningMessage(),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
					return false;
			}
			if(!mlistener.print())
				return false;
			mlistener.onChangeAction(miAction,ControlButtonListener.ACTION_SAVE);
		}
		miAction = ControlButtonListener.ACTION_NONE;
		setNormalState();
		mlistener.onChangeAction(ControlButtonListener.ACTION_SAVE,miAction);
		mlistener.clearBackup();
		return true;
	}
	////////////////////////////////////////////////////////
	public void cancel()
	{

		if(mlistener.isRestorable() && miAction != ControlButtonListener.ACTION_SEARCH && miAction != ControlButtonListener.ACTION_PRINT)

		{
			int iResult = MessageBox.showConfirmDialog(JOptionPane.getFrameForComponent(this),
				DefaultDictionary.getString("Confirm.SaveOnExit"),
				Global.APP_NAME, MessageBox.YES_NO_CANCEL_OPTION);
			if(iResult==MessageBox.YES_OPTION)
			{
				save();
				return;
 		}
			else if(iResult==MessageBox.CANCEL_OPTION)
				return;
		}



		mlistener.restore();
		mlistener.onChangeAction(miAction, ControlButtonListener.ACTION_CANCEL);
		miAction = 0;
		setNormalState();
		mlistener.onChangeAction(ControlButtonListener.ACTION_CANCEL,miAction);
	}
	////////////////////////////////////////////////////////
	public boolean exit()
	{
		if(miAction == ControlButtonListener.ACTION_ADD || miAction == ControlButtonListener.ACTION_MODIFY)

		{
			if(mlistener.isRestorable())
			{
				int iResult = MessageBox.showConfirmDialog(
					JOptionPane.getFrameForComponent(this),
					DefaultDictionary.getString("Confirm.SaveOnExit"),
					Global.APP_NAME,MessageBox.YES_NO_CANCEL_OPTION);
				if(iResult == MessageBox.YES_OPTION)
					return save();
				else if(iResult == MessageBox.CANCEL_OPTION)
					return false;
			}
		}
		return true;

		/*
		if(miAction == ControlButtonListener.ACTION_ADD ||
		   miAction == ControlButtonListener.ACTION_ADD_COPY ||
		   miAction == ControlButtonListener.ACTION_MODIFY)
		{
			if(mlistener.isChanged())
			{
				int iResult = MessageBox.showConfirmDialog(
								JOptionPane.getFrameForComponent(this),
								DefaultDictionary.getString("Confirm.SaveOnExit"),
								Global.APP_NAME,MessageBox.YES_NO_CANCEL_OPTION);
				if(iResult == MessageBox.YES_OPTION)
					return save();
				else if(iResult != MessageBox.NO_OPTION)
					return false;
			}
		}
		return true;*/
	}
	////////////////////////////////////////////////////////
	public int getCurrentAction()
	{
		return miAction;
	}
	////////////////////////////////////////////////////////
	public ControlButtonListener getControlButtonListener()
	{
		return mlistener;
	}
	////////////////////////////////////////////////////////
	public void setControlButtonListener(ControlButtonListener listener)
	{
		mlistener = listener;
	}
}
