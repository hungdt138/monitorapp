package com.fss.monitor;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.awt.event.*;
import java.util.*;

import com.fss.swing.*;
import com.fss.ddtp.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FPT - FSS</p>
 * @author SonNT
 * @version 1.0
 */

public class DialogLogViewer extends JXDialog
{
	private Dictionary mdic = null;
	////////////////////////////////////////////////////////
	private JButton btnDelete = new JButton();
	private JButton btnClose = new JButton();
	private JTextArea txtContent = new JTextArea();
	private JTree treeLog = null;
	////////////////////////////////////////////////////////
	JSplitPane splitPane = null;
	JScrollPane pnlLogFile = null;
	private JScrollPane pnlContent = null;
	private JPanel pnlButton = null;
	////////////////////////////////////////////////////////
	private String mstrTitle = null;
	private String mstrThreadID = null;
	private Vector mvtLogDir = null;
	private SocketTransmitter mChannel = null;
	////////////////////////////////////////////////////////
	private static final String mstrPleaseWait = "Data is loading, please wait ...";
	////////////////////////////////////////////////////////
	public DialogLogViewer(Component parent,String strThreadID,String strTitle,Vector vtLogDir,
							  SocketTransmitter channel) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		////////////////////////////////////////////////////////
		this.mstrThreadID = strThreadID;
		this.mstrTitle = strTitle;
		this.mvtLogDir = vtLogDir;
		this.mChannel = channel;
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(720,540);
	}
	////////////////////////////////////////////////////////
	public void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		DefaultMutableTreeNode topLog = new DefaultMutableTreeNode("Root log");
		createNodes(topLog);
		treeLog = new JTree(topLog);
		treeLog.putClientProperty("JTree.lineStyle", "Angled");
		treeLog.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeLog.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				DefaultMutableTreeNode node =
					(DefaultMutableTreeNode)treeLog.getLastSelectedPathComponent();

				if (node == null || node.isRoot()) return;
				if(node.isLeaf())
				{
					String nodeInfo = (String)node.getUserObject();
					showLog(nodeInfo);
				}
			}
		});
		JScrollPane pnlLogFile = new JScrollPane(treeLog);
		////////////////////////////////////////////////////////
		txtContent.setEditable(false);
		pnlContent = new JScrollPane(txtContent);
		pnlContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnlContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		////////////////////////////////////////////////////////
		pnlButton = new JPanel(new GridLayout(1,2,4,4));
		pnlButton.add(btnDelete);
		pnlButton.add(btnClose);
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlLogFile, pnlContent);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(160);
		splitPane.setBorder(BorderFactory.createEmptyBorder());

		pnlMain.add(splitPane,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		btnDelete.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onDelete();
			}
		});
		////////////////////////////////////////////////////////
		btnClose.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
	}

	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		mdic = MonitorDictionary.getChildDictionary("DialogLogViewer");
		MonitorDictionary.applyButton(btnDelete,"Delete");
		MonitorDictionary.applyButton(btnClose,"Close");
		setTitle(mdic.getString("Title") + ": " + mstrTitle);
	}

	////////////////////////////////////////////////////////
	public void createNodes(DefaultMutableTreeNode top)
	{
		DefaultMutableTreeNode logMonth = null;
		DefaultMutableTreeNode logNode = null;

		for(int iMonthIndex=0;iMonthIndex<mvtLogDir.size();iMonthIndex++)
		{
			Vector vtLogMonth = (Vector) mvtLogDir.elementAt(iMonthIndex);
			for(int iLogIndex=0;iLogIndex<vtLogMonth.size();iLogIndex++)
			{
				String strValue = (String) vtLogMonth.elementAt(iLogIndex);
				if(iLogIndex==0)
				{
					logMonth = new DefaultMutableTreeNode(strValue);
					top.add(logMonth);
				}
				else
				{
					logNode = new DefaultMutableTreeNode(strValue);
					logMonth.add(logNode);
				}
			}
		}
	}

	////////////////////////////////////////////////////////
	public void showLog(String strFileName)
	{
		txtContent.setText(mstrPleaseWait);
		treeLog.setEnabled(false);
		try
		{
			// Send command
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ThreadLogName",strFileName);
			DDTP response = mChannel.sendRequest("ThreadProcessor","loadThreadLogContent",request);
			if(response != null)
			{
				String strContent = response.getString("LogContent");
				txtContent.setText(strContent);
				txtContent.setCaretPosition(0);
			}
		}
		catch(Exception e)
		{
			txtContent.setText("");
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,"DialogLogViewer",MessageBox.ERROR_MESSAGE);
		}
		finally
		{
			treeLog.setEnabled(true);
		}
	}

	////////////////////////////////////////////////////////
	public void onDelete()
	{
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)treeLog.getLastSelectedPathComponent();
		if(node.isRoot())
			return;

		if(node.isLeaf())
		{
			if(MessageBox.OK_OPTION == MessageBox.showConfirmDialog(this,mdic.getString("DeleteOK"),"Delete confirm"))
			{
				treeLog.setEnabled(false);
				DefaultTreeModel model = (DefaultTreeModel)treeLog.getModel();
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
				String strFileName = (String)node.getUserObject();
				try
				{
					// Send command
					DDTP request = new DDTP();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					request.setString("ThreadID",mstrThreadID);
					request.setString("ThreadLogName",strFileName);
					DDTP response = mChannel.sendRequest("ThreadProcessor","deleteThreadLog",request);
					String str = response.getString("DeleteResult");
					txtContent.setText(str);
					model.removeNodeFromParent(node);

					if(parentNode.isLeaf())
						model.removeNodeFromParent(parentNode);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(this,e,"DialogLogViewer",MessageBox.ERROR_MESSAGE);
				}
				finally
				{
					treeLog.setEnabled(true);
				}
			}
		}
	}
}
