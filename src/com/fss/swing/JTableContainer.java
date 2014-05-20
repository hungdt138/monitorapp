package com.fss.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.fss.dictionary.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JTableContainer extends JPanel implements LanguageUpdatable
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public JPanel pnlStatus = new JPanel(new GridBagLayout());
	private JTextField txtIndexPerCount = new JTextField();
	private JScrollPane pnlContent;
	private JTable tblContent;
	private String mstrTitle = "";
	private boolean mbShowStatus = true;
	private JMenuItem mnuToggleStatus = new JMenuItem();
	////////////////////////////////////////////////////////
	// Contructor,destructor
	////////////////////////////////////////////////////////
	public JTableContainer(JTable tbl,String strTitle,boolean bShowStatus)
	{
		tblContent = tbl;
		if(tbl instanceof VectorTable)
		{
			((VectorTable)tbl).getPopupMenu().add(mnuToggleStatus);
			addMouseListener(((VectorTable)tbl).getPopupListener());
		}
		jbInit();
		setShowStatus(bShowStatus);
		setTitle(strTitle);
	}
	////////////////////////////////////////////////////////
	// Contructor,destructor
	////////////////////////////////////////////////////////
	public JTableContainer(JTable tbl)
	{
		this(tbl,"",false);
	}
	////////////////////////////////////////////////////////
	// Purpose: Init user interface
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	private void jbInit()
	{
		////////////////////////////////////////////////////////
		setBorder(BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,"",TitledBorder.CENTER,TitledBorder.DEFAULT_POSITION,Skin.FONT_LABEL));
		////////////////////////////////////////////////////////
		txtIndexPerCount.setFocusable(false);
		txtIndexPerCount.setBackground(UIManager.getColor("Panel.background"));
		txtIndexPerCount.setHorizontalAlignment(JTextField.RIGHT);
		pnlContent = new JScrollPane(tblContent);
		////////////////////////////////////////////////////////
		pnlStatus.add(txtIndexPerCount,new GridBagConstraints(0,0,GridBagConstraints.REMAINDER,GridBagConstraints.REMAINDER,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		mnuToggleStatus.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/status_bar.gif")));
		////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(pnlContent,BorderLayout.CENTER);
		updateLanguage();
		Skin.applySkin(mnuToggleStatus);
		Skin.applySkin(pnlStatus);
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		mnuToggleStatus.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setShowStatus(!getShowStatus());
			}
		});
		////////////////////////////////////////////////////////
		tblContent.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(mbShowStatus)
					displayTableInfo();
			}
		});
		////////////////////////////////////////////////////////
		tblContent.getModel().addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if(mbShowStatus)
					displayTableInfo();
			}
		});
	}
	////////////////////////////////////////////////////////
	// Purpose: Display table info
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	private void displayTableInfo()
	{
		int iSelected = tblContent.getSelectedRow() + 1;
		String strInfo = "Record: " + iSelected + "/" + tblContent.getRowCount();
		txtIndexPerCount.setText(strInfo);
	}
	////////////////////////////////////////////////////////
	// Purpose: Return content pane
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public JScrollPane getContentPane()
	{
		return pnlContent;
	}
	////////////////////////////////////////////////////////
	// Purpose: Return title
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public String getTitle()
	{
		return mstrTitle;
	}
	////////////////////////////////////////////////////////
	// Purpose: Set title
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public void setTitle(String strTitle)
	{
		mstrTitle = strTitle;
		((TitledBorder)getBorder()).setTitle(mstrTitle);
	}
	////////////////////////////////////////////////////////
	// Purpose: Return title
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public boolean getShowStatus()
	{
		return mbShowStatus;
	}
	////////////////////////////////////////////////////////
	// Purpose: Set title
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public void setShowStatus(boolean bShowStatus)
	{
		/*
		   if(mbShowStatus != bShowStatus)
		   {
		 mbShowStatus = bShowStatus;
		 if(mbShowStatus)
		 {
		  add(pnlStatus,BorderLayout.SOUTH);
		  displayTableInfo();
		 }
		 else
		  remove(pnlStatus);
		   updateLanguage();
		 validateTree();
		   }
		 */
		if (bShowStatus){
			add(pnlStatus,BorderLayout.SOUTH);
			displayTableInfo();
		} else
			 remove(pnlStatus);
		 updateLanguage();
		 validateTree();
	}
	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		if(getShowStatus())
			mnuToggleStatus.setText(DefaultDictionary.getString("JTableContainer.HideStatus"));
		else
			mnuToggleStatus.setText(DefaultDictionary.getString("JTableContainer.ShowStatus"));
	}
}
