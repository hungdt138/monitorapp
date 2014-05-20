package com.fss.monitor;

import java.net.*;
import java.awt.*;
import java.util.*;
import java.beans.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.html.*;

import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.swing.*;
import com.fss.thread.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: GUI of client</p>
 * <p>Description: Client communicate with server throw socket</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 *  - Thai Hoang Hiep
 *  - Dang Dinh Trung
 * @version 1.0
 */

public class PanelThreadManager extends JSplitPane implements LanguageChangeListener,PropertyChangeListener
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static int MAX_LOG_SIZE = 16384;
	public static Color COLOR_STARTED = new Color(150,170,180);
	public static Color COLOR_STARTED_FG = new Color(200,60,40);
	public boolean mbAutoLogIn = true;
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	private JButton chkVietnamese = new JButton();
	private Vector mvtLAFItem = new Vector();
	private Vector mvtLanguageItem = new Vector();
	private Vector mvtLanguage = new Vector();
	private JTabbedPane pnlThread = new JTabbedPane();
	private JSplitPane pnlUser = new JSplitPane();
	////////////////////////////////////////////////////////
	public JMenuBar mnuMain = new JMenuBar();
	private JMenu mnuSystem = new JMenu();
	private JMenuItem mnuSystem_Login = new JMenuItem();
	private JMenuItem mnuSystem_ChangePassword = new JMenuItem();
	private JMenuItem mnuSystem_StopServer = new JMenuItem();
	private JMenuItem mnuSystem_EnableThreads = new JMenuItem();
	private JMenu mnuHelp = new JMenu();
	private JMenuItem mnuHelp_About = new JMenuItem();
	private JMenu mnuUI = new JMenu();
	private UIManager.LookAndFeelInfo[] marrLaf = null;
	private JLabel lblStatus = new JLabel();
	private JPopupMenu pmn = new JPopupMenu();
	private JMenuItem mnuSelectAll = new JMenuItem();
	private JMenuItem mnuClearAll = new JMenuItem();
	private JMenuItem mnuClearSelected = new JMenuItem();
	////////////////////////////////////////////////////////
	public JEditorPane txtBoard = new JEditorPane();
	public JButton btnSend = new JButton();
	private JTextField txtMessage = new JTextField();
	private JButton btnKick = new JButton();
	private JButton btnRefresh = new JButton();
	private VectorTable tblUser = new VectorTable(4);
	////////////////////////////////////////////////////////
	public SocketTransmitter channel = null;
	private String mstrChannel = null;
	private String mstrThreadAppName = null;
	private String mstrThreadAppVersion = null;
	private String mstrAppName = null;
	private String mstrAppVersion = null;
	////////////////////////////////////////////////////////
	public PanelThreadManager()
	{
		try
		{
			Global.APP_NAME = ThreadConstant.APP_NAME;
			Global.APP_VERSION = ThreadConstant.APP_VERSION;
			jbInit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Init UI
	 * @throws java.lang.Exception
	 */
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		// Init LAF
		////////////////////////////////////////////////////////
		ButtonGroup grpLAF = new ButtonGroup();
		ActionListener lsnLAF = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				int iIndex = mvtLAFItem.indexOf(evt.getSource());
				if(iIndex >= 0)
					changeLAF(iIndex);
			}
		};
		////////////////////////////////////////////////////////
		UIManager.LookAndFeelInfo laf = new UIManager.LookAndFeelInfo("Kunststoff","com.incors.plaf.kunststoff.KunststoffLookAndFeel");
		marrLaf = UIManager.getInstalledLookAndFeels();
		int iIndex = 0;
		while(iIndex < marrLaf.length &&
			  !marrLaf[iIndex].getName().equals(laf.getName()))
			iIndex++;
		if(iIndex >= marrLaf.length)
		{
			UIManager.installLookAndFeel(laf);
			marrLaf = UIManager.getInstalledLookAndFeels();
		}
		for(iIndex = 0;iIndex < marrLaf.length;iIndex++)
		{
			JMenuItem mnu = new JRadioButtonMenuItem(marrLaf[iIndex].getName());
			mnu.addActionListener(lsnLAF);
			mvtLAFItem.addElement(mnu);
			mnuUI.add(mnu);
			grpLAF.add(mnu);
		}
		mnuUI.addSeparator();
		////////////////////////////////////////////////////////
		// Init language
		////////////////////////////////////////////////////////
		ButtonGroup grpLanguage = new ButtonGroup();
		ActionListener lsnLanguage = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int iIndex = mvtLanguageItem.indexOf(e.getSource());
				if(iIndex >= 0)
					changeDictionary((String)mvtLanguage.elementAt(iIndex));
			}
		};
		////////////////////////////////////////////////////////
		String[] str = MonitorDictionary.getSupportedLanguage();
		for(iIndex = 0;iIndex < str.length;iIndex++)
		{
			JMenuItem mnu = new JRadioButtonMenuItem(MonitorDictionary.getDictionary(str[iIndex]).getLanguage());
			mnu.addActionListener(lsnLanguage);
			mvtLanguage.addElement(str[iIndex]);
			mvtLanguageItem.addElement(mnu);
			mnuUI.add(mnu);
			grpLanguage.add(mnu);
		}
		////////////////////////////////////////////////////////
		// Add to main menu
		////////////////////////////////////////////////////////
		mnuMain.removeAll();
		mnuMain.add(mnuSystem);
		mnuSystem.add(mnuSystem_Login);
		mnuSystem.add(mnuSystem_ChangePassword);
		mnuSystem.addSeparator();
		mnuSystem.add(mnuSystem_StopServer);
		mnuSystem.add(mnuSystem_EnableThreads);
		mnuMain.add(mnuUI);
		mnuMain.add(mnuHelp);
		mnuHelp.add(mnuHelp_About);
		////////////////////////////////////////////////////////
		mnuMain.add(chkVietnamese);
		mnuMain.add(lblStatus);
		////////////////////////////////////////////////////////
		pnlThread.setTabPlacement(JTabbedPane.LEFT);
		pnlThread.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		////////////////////////////////////////////////////////
		tblUser.addColumn("",1,false);
		tblUser.addColumn("",2,false,Global.FORMAT_DATE_TIME);
		tblUser.addColumn("",3,false);
		////////////////////////////////////////////////////////
		JPanel pnlMessage = new JPanel();
		pnlMessage.setLayout(new GridBagLayout());
		pnlMessage.add(new JScrollPane(txtBoard),new GridBagConstraints(0,0,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMessage.add(txtMessage,new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMessage.add(btnSend,new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		txtBoard.setEditable(false);
		txtBoard.setAutoscrolls(true);
		txtBoard.setContentType("text/html");
		clearAll(txtBoard);
		////////////////////////////////////////////////////////
		JPanel pnlUserButton = new JPanel(new GridLayout(1,2,4,4));
		pnlUserButton.add(btnKick);
		pnlUserButton.add(btnRefresh);
		////////////////////////////////////////////////////////
		JPanel pnlManager = new JPanel(new GridBagLayout());
		pnlManager.add(new JScrollPane(tblUser),new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlManager.add(pnlUserButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
		////////////////////////////////////////////////////////
		pnlUser.setDividerLocation(320);
		pnlUser.setLeftComponent(pnlManager);
		pnlUser.setRightComponent(pnlMessage);
		pnlUser.setOneTouchExpandable(true);
		////////////////////////////////////////////////////////
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setOneTouchExpandable(true);
		pnlThread.setVisible(false);
		pnlUser.setVisible(false);
		setTopComponent(pnlThread);
		setBottomComponent(pnlUser);
		////////////////////////////////////////////////////////
		pmn.add(mnuSelectAll);
		pmn.addSeparator();
		pmn.add(mnuClearSelected);
		pmn.add(mnuClearAll);
		////////////////////////////////////////////////////////
		setBorder(BorderFactory.createEmptyBorder());
		pnlUser.setBorder(BorderFactory.createEmptyBorder());
		pnlManager.setBorder(BorderFactory.createBevelBorder(
			  javax.swing.border.BevelBorder.RAISED,Color.white,
			  UIManager.getColor("Panel.background"),
			  UIManager.getColor("Panel.background"),
			  UIManager.getColor("Panel.background")));
		pnlMessage.setBorder(BorderFactory.createBevelBorder(
			  javax.swing.border.BevelBorder.RAISED,Color.white,
			  UIManager.getColor("Panel.background"),
			  UIManager.getColor("Panel.background"),
			  UIManager.getColor("Panel.background")));
		////////////////////////////////////////////////////////
		Skin.applySkin(mnuMain);
		Skin.applySkin(tblUser);
		Skin.applySkin(pmn);
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		// Default setting
		////////////////////////////////////////////////////////
		Hashtable prt = null;
		try
		{
			prt = Global.loadHashtable(Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
			prt = new Hashtable();
		}
		changeLAF(Integer.parseInt(StringUtil.nvl(prt.get("LAF"),"0")));
		changeDictionary(StringUtil.nvl(prt.get("Language"),"VN"));
		Skin.LANGUAGE_CHANGE_LISTENER = this;
		MonitorProcessor.setRootObject(this);
		updateKeyboardUI();
		////////////////////////////////////////////////////////
		// Event handler
		////////////////////////////////////////////////////////
		tblUser.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() > 1)
					btnKick.doClick();
			}
		});
		////////////////////////////////////////////////////////
		btnRefresh.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					DDTP request = new DDTP();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					DDTP response = channel.sendRequest("ThreadProcessor","queryUserList",request);
					if(response != null)
					{
						tblUser.setData((Vector)response.getReturn());
						if(mstrChannel != null)
							removeUser(mstrChannel);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(pnlThread,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				}
			}
		});
		////////////////////////////////////////////////////////
		btnKick.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				int iSelected = tblUser.getSelectedRow();
				if(iSelected < 0)
					return;
				int iResult = MessageBox.showConfirmDialog(pnlThread,mdic.getString("ConfirmKick"),Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION)
					return;
				try
				{
					String strChannel = (String)tblUser.getRow(iSelected).elementAt(0);
					DDTP request = new DDTP();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					request.setString("strChannel",strChannel);
					DDTP response = channel.sendRequest("ThreadProcessor","kickUser",request);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(pnlThread,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				}
			}
		});
		////////////////////////////////////////////////////////
		txtMessage.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				btnSend.doClick();
			}
		});
		////////////////////////////////////////////////////////
		btnSend.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if(txtMessage.getText().length() == 0)
					return;
				try
				{
					DDTP request = new DDTP();
					request.setString("strMessage",txtMessage.getText());
					channel.sendRequest("ThreadProcessor","sendMessage",request);
					txtMessage.setText("");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(pnlThread,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				}
			}
		});
		////////////////////////////////////////////////////////
		mnuClearAll.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clearAll(txtBoard);
			}
		});
		////////////////////////////////////////////////////////
		mnuClearSelected.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtBoard.setEditable(true);
				txtBoard.replaceSelection("");
				txtBoard.setEditable(false);
			}
		});
		////////////////////////////////////////////////////////
		mnuSelectAll.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtBoard.requestFocus();
				txtBoard.selectAll();
			}
		});
		////////////////////////////////////////////////////////
		txtBoard.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == e.BUTTON3)
					pmn.show(txtBoard,e.getX(),e.getY());
			}
		});
		////////////////////////////////////////////////////////
		mnuSystem_Login.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				login();
			}
		});
		////////////////////////////////////////////////////////
		mnuSystem_ChangePassword.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				changePassword();
			}
		});
		////////////////////////////////////////////////////////
		mnuSystem_StopServer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopServer();
			}
		});
		////////////////////////////////////////////////////////
		mnuSystem_EnableThreads.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				manageThreads();
			}
		});
		////////////////////////////////////////////////////////
		mnuHelp_About.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				WindowManager.centeredWindow(new DialogAbout(PanelThreadManager.this));
			}
		});
		////////////////////////////////////////////////////////
		chkVietnamese.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				switchKeyboard();
			}
		});
	}
	//////////////////////////////////////////////////////////////
	/**
	 *
	 * @param txt JEditorPane
	 */
	//////////////////////////////////////////////////////////////
	public static void clearAll(JEditorPane txt)
	{
		try
		{
			txt.setText("");
			HTMLEditorKit kit = (HTMLEditorKit)txt.getEditorKit();
			HTMLDocument doc = (HTMLDocument)txt.getDocument();
			kit.insertHTML(doc,0,"<body style=\"font-family:'Courier New';font-size: 12pt;\">",0,0,null);
		}
		catch(Exception e)
		{
		}
	}
	//////////////////////////////////////////////////////////////
	/**
	 * Load menu
	 */
	//////////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		//////////////////////////////////////////////////////////////
		mdic = MonitorDictionary.getChildDictionary("ThreadManager");
		MonitorDictionary.applyButton(btnKick,"Kick");
		MonitorDictionary.applyButton(btnRefresh,"Refresh");
		MonitorDictionary.applyButton(btnSend,"Send");
		tblUser.setColumnNameEx(mdic.getString("LoginName"),1);
		tblUser.setColumnNameEx(mdic.getString("LoginTime"),2);
		tblUser.setColumnNameEx(mdic.getString("Host"),3);
		//////////////////////////////////////////////////////////////
		MonitorDictionary.applyButton(mnuSelectAll,"jmenu.Edit.SelectAll");
		MonitorDictionary.applyButton(mnuClearAll,"jmenu.Edit.ClearAll");
		MonitorDictionary.applyButton(mnuClearSelected,"jmenu.Edit.ClearSelected");
		//////////////////////////////////////////////////////////////
		MonitorDictionary.applyButton(mnuSystem,"jmenu.System");
		MonitorDictionary.applyButton(mnuSystem_ChangePassword,"jmenu.System.ChangePassword");
		MonitorDictionary.applyButton(mnuSystem_StopServer,"jmenu.System.Shutdown");
		MonitorDictionary.applyButton(mnuSystem_EnableThreads,"jmenu.System.ThreadManager");
		MonitorDictionary.applyButton(mnuHelp,"jmenu.Help");
		MonitorDictionary.applyButton(mnuHelp_About,"jmenu.Help.About");
		//////////////////////////////////////////////////////////////
		MonitorDictionary.applyButton(mnuUI,"jmenu.UI");
		//////////////////////////////////////////////////////////////
		if(isOpen())
		{
			lblStatus.setText("  " + MonitorDictionary.getString("LoggedUser") + ": " + channel.getUserName());
			MonitorDictionary.applyButton(mnuSystem_Login,"jmenu.System.Logout");
			mnuSystem_ChangePassword.setEnabled(true);
			mnuSystem_StopServer.setEnabled(true);
			mnuSystem_EnableThreads.setEnabled(true);
		}
		else
		{
			lblStatus.setText("");
			MonitorDictionary.applyButton(mnuSystem_Login,"jmenu.System.Login");
			mnuSystem_ChangePassword.setEnabled(false);
			mnuSystem_StopServer.setEnabled(false);
			mnuSystem_EnableThreads.setEnabled(false);
		}
		//////////////////////////////////////////////////////////////
		for(int iIndex = 0;iIndex < pnlThread.getTabCount();iIndex++)
			((PanelThreadMonitor)pnlThread.getComponentAt(iIndex)).updateLanguage();
	}
	////////////////////////////////////////////////////////
	/**
	 * Change laf
	 * @param iLAFIndex laf index
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void changeLAF(int iLAFIndex)
	{
		try
		{
			// Change LAF
			if(iLAFIndex >= marrLaf.length)
				iLAFIndex = marrLaf.length - 1;
			UIManager.setLookAndFeel((LookAndFeel)Class.forName(marrLaf[iLAFIndex].getClassName()).newInstance());

			// Update UI
			((JMenuItem)mvtLAFItem.elementAt(iLAFIndex)).setSelected(true);
			SwingUtilities.updateComponentTreeUI(this);
			SwingUtilities.updateComponentTreeUI(mnuMain);
			WindowManager.updateLookAndField();

			// Store config
			try
			{
				Hashtable prt = Global.loadHashtable(Global.FILE_CONFIG);
				prt.put("LAF",String.valueOf(iLAFIndex));
				Global.storeHashtable(prt,Global.FILE_CONFIG);
			}
			catch(Exception e)
			{
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Handle change keyboard event
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void switchKeyboard()
	{
		if(Skin.VIETNAMESE_KEY.isEnabled())
			Skin.VIETNAMESE_KEY.setEnabled(false);
		else
			Skin.VIETNAMESE_KEY.setEnabled(true);
		updateKeyboardUI();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void updateKeyboardUI()
	{
		if(Skin.VIETNAMESE_KEY.isEnabled())
		{
			chkVietnamese.setText(" V ");
			chkVietnamese.setBackground(Color.yellow);
			chkVietnamese.setBorder(BorderFactory.createLineBorder(Color.red,1));
		}
		else
		{
			chkVietnamese.setText(" E ");
			chkVietnamese.setBackground(Color.cyan);
			chkVietnamese.setBorder(BorderFactory.createLineBorder(Color.blue,1));
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Handle change dictionary event
	 * @param strLanguage language to change
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void changeDictionary(String strLanguage)
	{
		// Change dictionary
		MonitorDictionary.setCurrentLanguage(strLanguage);
		DefaultDictionary.setCurrentLanguage(strLanguage);
		ErrorDictionary.setCurrentLanguage(strLanguage);

		// Update UI
		updateLanguage();
		WindowManager.updateLanguage();
		int iIndex = mvtLanguage.indexOf(strLanguage);
		if(iIndex >= 0)
		{
			JRadioButtonMenuItem mnu = (JRadioButtonMenuItem)mvtLanguageItem.elementAt(iIndex);
			mnu.setSelected(true);
		}

		// Store config
		Hashtable prt = null;
		try
		{
			prt = Global.loadHashtable(Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
			prt = new Hashtable();
		}
		prt.put("Language",strLanguage);
		try
		{
			Global.storeHashtable(prt,Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: send message stop server
	// Author: TrungDD
	// Date: 10/2003
	////////////////////////////////////////////////////////
	public void stopServer()
	{
		if(MessageBox.showConfirmDialog(this,MonitorDictionary.getString("Confirm.Shutdown"),Global.APP_NAME,MessageBox.YES_NO_OPTION) == MessageBox.NO_OPTION)
			return;

		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			channel.sendRequest("ThreadProcessor","closeServer",request);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Control login to system
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void login()
	{
		mbAutoLogIn = false;
		while(!mbAutoLogIn)
		{
			try
			{
				// Confirm logout
				if(isOpen())
				{
					if(MessageBox.showConfirmDialog(this,MonitorDictionary.getString("Confirm.Exit"),Global.APP_NAME,MessageBox.YES_NO_OPTION) == MessageBox.NO_OPTION)
						return;

					// Disconnect from server
					disconnect();
				}

				// Login
				DialogLogin dlgLogin = new DialogLogin(this);
				WindowManager.centeredWindow(dlgLogin);

				if(dlgLogin.miReturn == JOptionPane.OK_OPTION)
				{
					// Update UI
					SwingUtilities.updateComponentTreeUI(pnlUser);
					SwingUtilities.updateComponentTreeUI(pnlThread);

					// Request to connect
					Socket sck = new Socket(dlgLogin.mstrHost,Integer.parseInt(dlgLogin.mstrPort));
					sck.setSoLinger(true,0);

					// Start up a channel thread that reads messages from the server
					channel = new SocketTransmitter(sck)
					{
						public void close()
						{
							if(msckMain != null)
							{
								super.close();
								closeAll();
								if(mbAutoLogIn)
									login();
							}
						}
					};
					channel.setUserName(dlgLogin.mstrUserName);
					channel.setPackage("com.fss.thread.");
					channel.start();

					// Request to Server
					DDTP request = new DDTP();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					request.setString("UserName",dlgLogin.mstrUserName);
					request.setString("Password",dlgLogin.mstrPassword);

					// Response from Server
					DDTP response = channel.sendRequest("ThreadProcessor","login",request);
					mstrThreadAppName = response.getString("strThreadAppName");
					mstrThreadAppVersion = response.getString("strThreadAppVersion");
					mstrAppName = response.getString("strAppName");
					mstrAppVersion = response.getString("strAppVersion");
					if(response != null)
					{
						if(response.getString("PasswordExpired") != null)
						{
							DialogChangePassword frm = new DialogChangePassword(this,channel);
							WindowManager.centeredWindow(frm);
							if(frm.miReturnValue != JOptionPane.OK_OPTION)
								throw new AppException("FSS-10003");
						}
						String strLog = StringUtil.nvl(response.getString("strLog"),"");
						showResult(txtBoard,strLog);
						Vector vtThread = response.getVector("vtThread");
						updateTabBar(vtThread);

						mstrChannel = response.getString("strChannel");
						if(mstrChannel != null)
							removeUser(mstrChannel);
					}
					btnRefresh.doClick();
					pnlThread.setVisible(true);
					pnlUser.setVisible(true);
					setResizeWeight(1);
					setDividerLocation((int)(getSize().getHeight() - 160));
				}
				mbAutoLogIn = true;
				updateLanguage();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);

				// Disconnect from server
				mstrChannel = null;
				disconnect();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getThreadAppName()
	{
		return StringUtil.nvl(mstrThreadAppName,"Unknown platform");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getThreadAppVersion()
	{
		return StringUtil.nvl(mstrThreadAppVersion,"Unknown version");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getAppName()
	{
		return StringUtil.nvl(mstrAppName,"Unknown application");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getAppVersion()
	{
		return StringUtil.nvl(mstrAppVersion,"Unknown version");
	}
	//////////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void updateUI()
	{
		super.updateUI();
		if(pmn != null)
		{
			SwingUtilities.updateComponentTreeUI(pmn);
			Skin.applySkin(pmn);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Disconnect from server
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void disconnect()
	{
		if(channel != null)
			channel.close();
	}
	////////////////////////////////////////////////////////
	/**
	 * Remove all component
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	private synchronized void closeAll()
	{
		try
		{
			// Close all child window
			WindowManager.closeAll();

			// Remove all child component
			pnlThread.setVisible(false);
			pnlUser.setVisible(false);
			pnlThread.removeAll();
			clearAll(txtBoard);
			tblUser.setData(new Vector());
			Thread.sleep(500);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Called first time when logined to System sucessfully
	 * @param vtThread list of running thread
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	private void updateTabBar(Vector vtThread)
	{
		pnlThread.removeAll();
		for(int iThreadIndex = 0;iThreadIndex < vtThread.size();iThreadIndex++)
		{
			try
			{
				Vector vtThreadInfo = (Vector)vtThread.elementAt(iThreadIndex);
				PanelThreadMonitor mntTemp = new PanelThreadMonitor(channel);

				String strThreadID = (String)vtThreadInfo.elementAt(0);
				String strThreadName = (String)vtThreadInfo.elementAt(1);
				int iThreadStatus = Integer.parseInt((String)vtThreadInfo.elementAt(2));
				mntTemp.setThreadID(strThreadID);
				mntTemp.setThreadName(strThreadName);
				mntTemp.setThreadStatus(iThreadStatus);
				showResult(mntTemp.txtMonitor,(String)vtThreadInfo.elementAt(3));
				mntTemp.addPropertyChangeListener(this);

				pnlThread.add(strThreadName,mntTemp);
				mntTemp.updateStatus();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			}
		}
		Skin.applySkin(this);
	}
	////////////////////////////////////////////////////////
	/**
	 * Show result of log to monitor
	 * @param txt monitor
	 * @param result log content
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void showResult(JEditorPane txt,String result)
	{
		try
		{
			if(txt.getText().length() == 0)
			{
				appendHTML(txt,result);
				txt.setSelectionStart(txt.getText().length());
				txt.setSelectionEnd(txt.getText().length() - 1);
			}
			else
			{
				if(txt.getText().length() > MAX_LOG_SIZE)
				{
					txt.setSelectionStart(0);
					txt.setSelectionStart(txt.getText().length() - MAX_LOG_SIZE);
					txt.replaceSelection("");
				}
				txt.setSelectionStart(txt.getText().length());
				appendHTML(txt,result);
				txt.setSelectionEnd(txt.getText().length());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Show result of log to monitor
	 * @param txt monitor
	 * @param result log content
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public static void showResult(JTextArea txt,String result)
	{
		try
		{
			if(txt.getText().length() == 0)
			{
				txt.setText(result);
				txt.setSelectionStart(txt.getText().length());
				txt.setSelectionEnd(txt.getText().length() - 1);
			}
			else
			{
				if(txt.getText().length() > MAX_LOG_SIZE)
					txt.getDocument().remove(0,txt.getText().length() - MAX_LOG_SIZE);
				txt.setSelectionStart(txt.getText().length());
				txt.getDocument().insertString(txt.getText().length(),result,null);
				txt.setSelectionEnd(txt.getText().length());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param editor JEditorPane
	 * @param html String
	 */
	////////////////////////////////////////////////////////
	private static void appendHTML(JEditorPane editor,String html)
	{
		try
		{
			html = StringUtil.replaceAll(html,"\t","&nbsp;&nbsp;&nbsp;&nbsp;");
			html = StringUtil.replaceAll(html,"\r\n","\n");
			html = StringUtil.replaceAll(html,"\r","");
			Vector vt = StringUtil.toStringVector(html,"\n");
			HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
			HTMLDocument doc = (HTMLDocument)editor.getDocument();
			for(int iIndex = 0;iIndex < vt.size();iIndex++)
				kit.insertHTML(doc,doc.getLength(),(String)vt.elementAt(iIndex),0,0,null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Update thread status
	 * @param strThreadID thread id
	 * @param status status
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void updateStatus(String strThreadID,String status)
	{
		PanelThreadMonitor pnlThrMonitor = getPanelThreadMonitor(strThreadID);
		int iStatus = Integer.parseInt(status);
		if (pnlThrMonitor!=null)
		{
			pnlThrMonitor.setThreadStatus(iStatus);
			pnlThrMonitor.updateStatus();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * delete one panel corresponse to one disabled thread
	 * @param strThreadID thread id
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void unloadThread(String strThreadID)
	{
		PanelThreadMonitor pnlThrMonitor = getPanelThreadMonitor(strThreadID);
		pnlThread.remove(pnlThrMonitor);
	}
	////////////////////////////////////////////////////////
	/**
	 * insert one panel corresponse to one new enabled thread
	 * @param threadID thread id
	 * @param threadName thread name
	 * @param threadStatus status
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void loadThread(String threadID,String threadName,String threadStatus)
	{
		try
		{
			PanelThreadMonitor mntTemp = new PanelThreadMonitor(channel);
			int iThreadStatus = Integer.parseInt(threadStatus);
			mntTemp.setThreadID(threadID);
			mntTemp.setThreadName(threadName);
			mntTemp.setThreadStatus(iThreadStatus);
			mntTemp.addPropertyChangeListener(this);

			pnlThread.add(threadName,mntTemp);
			mntTemp.updateStatus();
			Skin.applySkin(mntTemp);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Get PanelThreadMonitor in list
	 * @param strThreadID thread id
	 * @return PanelThreadMonitor found, null if not found
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	PanelThreadMonitor getPanelThreadMonitor(String strThreadID)
	{
		Component[] cmp = pnlThread.getComponents();
		for(int iMonitorIndex = 0;iMonitorIndex < cmp.length;iMonitorIndex++)
		{
			if(cmp[iMonitorIndex] instanceof PanelThreadMonitor)
			{
				PanelThreadMonitor mntTemp = ((PanelThreadMonitor)cmp[iMonitorIndex]);
				if(mntTemp.mstrThreadID.equals(strThreadID))
					return mntTemp;
			}
		}
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first index of thread in thread list
	 * @param strThreadID thread id
	 * @return first index of thread in thread list
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	int indexOf(String strThreadID)
	{
		int iMonitorCount = pnlThread.getComponentCount();
		for(int iMonitorIndex = 0;iMonitorIndex < iMonitorCount;iMonitorIndex++)
		{
			PanelThreadMonitor mntTemp = ((PanelThreadMonitor)pnlThread.getComponentAt(iMonitorIndex));
			if(mntTemp.mstrThreadID.equals(strThreadID))
				return iMonitorIndex;
		}
		return -1;
	}
	/////////////////////////////////////////////////////////////
	/**
	 * Change password of this user
	 * @author TrungDD
	 */
	/////////////////////////////////////////////////////////////
	public void changePassword()
	{
		try
		{
			DialogChangePassword dlgPass = new DialogChangePassword(this,channel);
			if(dlgPass != null)
				WindowManager.centeredWindow(dlgPass);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Rename thread
	 * @param strThreadID ThreadID
	 * @param strThreadName New Name
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void renameThread(String strThreadID,String strThreadName)
	{
		PanelThreadMonitor pnlThrMonitor = getPanelThreadMonitor(strThreadID);
		pnlThrMonitor.setThreadName(strThreadName);
		pnlThread.setTitleAt(indexOf(strThreadID),strThreadName);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add user
	 * @param strChannel String
	 * @param strUserName String
	 * @param strConnectDate String
	 * @param strHost String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void addUser(String strChannel,String strUserName,String strConnectDate,String strHost)
	{
		if(strChannel.equals(mstrChannel))
			return;
		removeUser(strChannel);
		Vector vtData = new Vector();
		vtData.addElement(strChannel);
		vtData.addElement(strUserName);
		vtData.addElement(strConnectDate);
		vtData.addElement(strHost);
		tblUser.addRow(vtData);
	}
	////////////////////////////////////////////////////////
	/**
	 * Remove user
	 * @param strChannel String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void removeUser(String strChannel)
	{
		Vector vtData = tblUser.getFilteredData();
		for(int iIndex = 0;iIndex < vtData.size();iIndex++)
		{
			Vector vtRow = (Vector)vtData.elementAt(iIndex);
			if(vtRow.elementAt(0).equals(strChannel))
				tblUser.deleteRow(iIndex);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * call DialogThreadManager
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public void manageThreads()
	{
		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			DDTP response = channel.sendRequest("ThreadProcessor","manageThreadsLoad",request);
			if(response!=null)
			{
				DialogThreadManager dlg = new DialogThreadManager(this,channel,response.getVector("vtTableData"));
				dlg.setComboStartupType(response.getVector("vtStartupType"),"");
				WindowManager.centeredWindow(dlg);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Test connection
	 * @return true if connected
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public boolean isOpen()
	{
		return (channel != null && channel.isOpen());
	}
	////////////////////////////////////////////////////////
	/**
	 * Handle property change event
	 * @param e event to handle
	 */
	////////////////////////////////////////////////////////
	public void propertyChange(PropertyChangeEvent e)
	{
		if(e.getSource() instanceof PanelThreadMonitor)
		{
			if(e.getPropertyName().equals("Status"))
			{
				PanelThreadMonitor mntTmp = (PanelThreadMonitor)e.getSource();
				int iMonitorIndex = pnlThread.indexOfComponent(mntTmp);
				if(iMonitorIndex >= 0)
				{
					if(e.getNewValue().equals("Started"))
					{
						if(!pnlThread.getBackgroundAt(iMonitorIndex).equals(COLOR_STARTED))
						{
							pnlThread.setBackgroundAt(iMonitorIndex,COLOR_STARTED);
							pnlThread.setForegroundAt(iMonitorIndex,COLOR_STARTED_FG);
						}
					}
					else if(e.getNewValue().equals("Stopped"))
					{
						if(!pnlThread.getBackgroundAt(iMonitorIndex).equals(SystemColor.controlShadow))
						{
							pnlThread.setBackgroundAt(iMonitorIndex,UIManager.getColor("TabbedPane.background"));
							pnlThread.setForegroundAt(iMonitorIndex,SystemColor.textText);
						}
					}
				}
			}
		}
	}
}
