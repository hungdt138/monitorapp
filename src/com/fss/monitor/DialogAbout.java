package com.fss.monitor;

import java.awt.*;
import javax.swing.*;

import com.fss.swing.*;
import com.fss.thread.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DialogAbout extends JXDialog
{
	////////////////////////////////////////////////////////
	private PanelThreadManager parent;
	public DialogAbout(PanelThreadManager parent)
	{
		super(parent,true);
		this.parent = parent;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("About " + ThreadConstant.APP_NAME);
		try
		{
			jbInit();
			this.pack();
			this.setSize(this.getHeight() * 4 / 3,this.getHeight());
			this.setResizable(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void jbInit()
	{
		JPanel pnlUI = new JPanel(new GridLayout(2,1,4,4));
		pnlUI.setBorder(BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,"User interface"));
		pnlUI.add(new JLabel(ThreadConstant.APP_NAME,JLabel.CENTER));
		pnlUI.add(new JLabel("Version " + ThreadConstant.APP_VERSION,JLabel.CENTER));

		JPanel pnlPlatform = new JPanel(new GridLayout(2,1,4,4));
		pnlPlatform.setBorder(BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,"Thread platform"));
		pnlPlatform.add(new JLabel(parent.getThreadAppName(),JLabel.CENTER));
		pnlPlatform.add(new JLabel("Version " + parent.getThreadAppVersion(),JLabel.CENTER));

		JPanel pnlApplication = new JPanel(new GridLayout(2,1,4,4));
		pnlApplication.setBorder(BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,"Application"));
		pnlApplication.add(new JLabel(parent.getAppName(),JLabel.CENTER));
		pnlApplication.add(new JLabel("Version " + parent.getAppVersion(),JLabel.CENTER));

		this.getContentPane().setLayout(new GridBagLayout());
		this.getContentPane().add(pnlUI,new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		this.getContentPane().add(pnlPlatform,new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		this.getContentPane().add(pnlApplication,new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		Skin.applySkin(this);
	}
}
