package com.fss.monitor;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.swing.*;
import com.fss.dictionary.*;
import com.fss.thread.ScheduleUtil;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DialogSchedule extends JXDialog implements ControlButtonListener
{
	////////////////////////////////////////////////////////
	// Label
	////////////////////////////////////////////////////////
	Dictionary mdic = null;
	////////////////////////////////////////////////////////
	// Label
	////////////////////////////////////////////////////////
	private JLabel lblExecutionTime = new JLabel();
	private JLabel lblAdditionValue = new JLabel();
	private JLabel lblStartTime = new JLabel();
	private JLabel lblEndTime = new JLabel();
	private JLabel lblExpectedDate = new JLabel();
	private JLabel lblScheduleType = new JLabel();
	private JLabel lblExecutionTimeDesc = new JLabel();
	private JLabel lblAdditionValueDesc = new JLabel();
	////////////////////////////////////////////////////////
	// Filter
	////////////////////////////////////////////////////////
	private VectorTable tblWeekDay = new VectorTable(3);
	private VectorTable tblMonthDay = new VectorTable(2);
	private VectorTable tblYearMonth = new VectorTable(3);
	////////////////////////////////////////////////////////
	private Dictionary mdicSchedule;
	////////////////////////////////////////////////////////
	// Schedule specific
	////////////////////////////////////////////////////////
	private JXCombo cboScheduleType = new JXCombo();
	private JXText txtExecutionTime = new JXText();
	private JXText txtAdditionValue = new JXText();
	private JXText txtStartTime = new JXText();
	private JXText txtEndTime = new JXText();
	private JXDatePlus txtExpectedDate = new JXDatePlus();
	////////////////////////////////////////////////////////
	// Schedule list
	////////////////////////////////////////////////////////
	private VectorTable tblSchedule = new VectorTable(5);
	private JTableContainer pnlSchedule = new JTableContainer(tblSchedule);
	////////////////////////////////////////////////////////
	// Others
	////////////////////////////////////////////////////////
	private TitledBorder bdrInput = BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,"");
	private JPanel pnlInput = new JPanel(new GridBagLayout());
	private JPanel pnlFilter = new JPanel(new GridBagLayout());
	private PanelControlButton pnlButton = new PanelControlButton(this);
	private String mstrThreadID = null;
	private Transmitter channel = null;
	////////////////////////////////////////////////////////
	public DialogSchedule(Component parent,String strThreadID,Transmitter channel) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		mstrThreadID = strThreadID;
		this.channel = channel;
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		pnlButton.setAllowSearch(false);
		onChangeAction(ACTION_NONE,ACTION_NONE);
		pnlButton.setNormalState();
		search();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setSize(624,480);
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		tblSchedule.addColumn("",0,false,new java.text.DecimalFormat());
		tblSchedule.addColumn("",1,false);
		tblSchedule.addColumn("",2,false);
		tblSchedule.addColumn("",3,false);
		tblSchedule.getColumnEx(0).setPreferredWidth(50);
		tblSchedule.getColumnEx(1).setPreferredWidth(400);
		tblSchedule.getColumnEx(2).setPreferredWidth(150);
		tblSchedule.getColumnEx(3).setPreferredWidth(150);
		////////////////////////////////////////////////////////
		tblWeekDay.addColumn("",0,false);
		tblWeekDay.addColumn("+",1,true);
		tblWeekDay.setColumnEditorEx(new JCheckBox(),1);
		tblWeekDay.getColumnEx(0).setPreferredWidth(200);
		tblWeekDay.getColumnEx(1).setPreferredWidth(20);
		tblWeekDay.setHeaderSort(false);
		tblWeekDay.setAllowFilter(false);
		////////////////////////////////////////////////////////
		tblMonthDay.addColumn("",0,false,Global.FORMAT_NUMBER);
		tblMonthDay.addColumn("+",1,true);
		tblMonthDay.setColumnAlignEx(VectorTable.ALIGN_RIGHT,0);
		tblMonthDay.setColumnEditorEx(new JCheckBox(),1);
		tblMonthDay.getColumnEx(0).setPreferredWidth(200);
		tblMonthDay.getColumnEx(1).setPreferredWidth(20);
		tblMonthDay.setHeaderSort(false);
		tblMonthDay.setAllowFilter(false);
		////////////////////////////////////////////////////////
		tblYearMonth.addColumn("",0,false);
		tblYearMonth.addColumn("+",1,true);
		tblYearMonth.setColumnEditorEx(new JCheckBox(),1);
		tblYearMonth.getColumnEx(0).setPreferredWidth(200);
		tblYearMonth.getColumnEx(1).setPreferredWidth(20);
		tblYearMonth.setHeaderSort(false);
		tblYearMonth.setAllowFilter(false);
		////////////////////////////////////////////////////////
		cboScheduleType.addItem("Daily");
		cboScheduleType.addItem("Weekly");
		cboScheduleType.addItem("Monthly");
		cboScheduleType.addItem("Yearly");
		////////////////////////////////////////////////////////
		txtAdditionValue.setMask("9990");
		txtExecutionTime.setMask("9990");
		txtStartTime.setMask("90:90:90");
		txtEndTime.setMask("90:90:90");
		////////////////////////////////////////////////////////
		pnlFilter.add(new JScrollPane(tblMonthDay),new GridBagConstraints(0,0,1,1,0.6,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		pnlFilter.add(new JScrollPane(tblWeekDay),new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,2,0,2),0,0));
		pnlFilter.add(new JScrollPane(tblYearMonth),new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		////////////////////////////////////////////////////////
		pnlInput.setBorder(bdrInput);
		pnlInput.add(lblScheduleType,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(cboScheduleType,new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblExpectedDate,new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(txtExpectedDate,new GridBagConstraints(3,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblAdditionValue,new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(txtAdditionValue,new GridBagConstraints(5,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblAdditionValueDesc,new GridBagConstraints(6,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblStartTime,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(txtStartTime,new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblEndTime,new GridBagConstraints(2,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(txtEndTime,new GridBagConstraints(3,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblExecutionTime,new GridBagConstraints(4,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(txtExecutionTime,new GridBagConstraints(5,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlInput.add(lblExecutionTimeDesc,new GridBagConstraints(6,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlInput.add(pnlFilter,new GridBagConstraints(0,2,7,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(4,0,0,0),0,0));
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(pnlInput,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
		pnlMain.add(pnlSchedule,new GridBagConstraints(0,2,1,1,1.0,1.38,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		tblSchedule.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() > 1)
					pnlButton.btnModify.doClick();
			}
		});
		////////////////////////////////////////////////////////
		tblWeekDay.getTableHeader().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if(onHeaderClick(tblWeekDay,evt.getX()))
					evt.consume();
			}
		});
		////////////////////////////////////////////////////////
		tblMonthDay.getTableHeader().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if(onHeaderClick(tblMonthDay,evt.getX()))
					evt.consume();
			}
		});
		////////////////////////////////////////////////////////
		tblYearMonth.getTableHeader().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if(onHeaderClick(tblYearMonth,evt.getX()))
					evt.consume();
			}
		});
		////////////////////////////////////////////////////////
		cboScheduleType.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == e.SELECTED)
					onChangeScheduleType();
			}
		});
		////////////////////////////////////////////////////////
		tblSchedule.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				fillDetailValue();
			}
		});
	}
	////////////////////////////////////////////////////////
	public boolean onHeaderClick(VectorTable tbl,int iPosition)
	{
		if(!tbl.isEnabled())
			return false;
		if(tbl.getRowCount() <= 0)
			return false;
		int iColumnIndex = tbl.getColumnModel().getColumnIndexAtX(iPosition);
		if(tbl.getColumn(iColumnIndex).getModelIndex() == 1)
		{
			String strValue = "FALSE";
			if(tbl.getRow(0).elementAt(1).equals(strValue))
				strValue = "TRUE";
			tbl.setAllValueEx(1,strValue);
			return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////
	public void updateLanguage() throws Exception
	{
		////////////////////////////////////////////////////////
		mdic = MonitorDictionary.getChildDictionary("DialogSchedule");
		////////////////////////////////////////////////////////
		tblSchedule.setColumnNameEx(mdic.getString("ID"),0);
		tblSchedule.setColumnNameEx(mdic.getString("Description"),1);
		tblSchedule.setColumnNameEx(mdic.getString("NextExecution"),2);
		tblSchedule.setColumnNameEx(mdic.getString("ExecutionCount"),3);
		////////////////////////////////////////////////////////
		lblExecutionTime.setText(mdic.getString("ExecutionTime"));
		lblExecutionTimeDesc.setText(mdic.getString("TimePerDay"));
		lblAdditionValue.setText(mdic.getString("AdditionValue"));
		lblStartTime.setText(mdic.getString("StartTime"));
		lblEndTime.setText(mdic.getString("EndTime"));
		lblExpectedDate.setText(mdic.getString("ExpectedDate"));
		lblScheduleType.setText(mdic.getString("ScheduleType"));
		////////////////////////////////////////////////////////
		while(tblWeekDay.getRowCount() < 7)
			tblWeekDay.addRow();
		while(tblWeekDay.getRowCount() > 7)
			tblWeekDay.deleteRow(0);
		for(int iIndex = 0;iIndex < tblWeekDay.getRowCount();iIndex++)
			tblWeekDay.getRow(iIndex).setElementAt(String.valueOf(iIndex + 1),2);
		tblWeekDay.getRow(0).setElementAt(mdic.getString("Sun"),0);
		tblWeekDay.getRow(1).setElementAt(mdic.getString("Mon"),0);
		tblWeekDay.getRow(2).setElementAt(mdic.getString("Tue"),0);
		tblWeekDay.getRow(3).setElementAt(mdic.getString("Wed"),0);
		tblWeekDay.getRow(4).setElementAt(mdic.getString("Thu"),0);
		tblWeekDay.getRow(5).setElementAt(mdic.getString("Fri"),0);
		tblWeekDay.getRow(6).setElementAt(mdic.getString("Sat"),0);
		////////////////////////////////////////////////////////
		while(tblMonthDay.getRowCount() < 31)
			tblMonthDay.addRow();
		while(tblMonthDay.getRowCount() > 31)
			tblMonthDay.deleteRow(0);
		for(int iIndex = 0;iIndex < tblMonthDay.getRowCount();iIndex++)
			tblMonthDay.getRow(iIndex).setElementAt(String.valueOf(iIndex + 1),0);
		////////////////////////////////////////////////////////
		while(tblYearMonth.getRowCount() < 12)
			tblYearMonth.addRow();
		while(tblYearMonth.getRowCount() > 12)
			tblYearMonth.deleteRow(0);
		for(int iIndex = 0;iIndex < tblYearMonth.getRowCount();iIndex++)
			tblYearMonth.getRow(iIndex).setElementAt(String.valueOf(iIndex),2);
		tblYearMonth.getRow(0).setElementAt(mdic.getString("Jan"),0);
		tblYearMonth.getRow(1).setElementAt(mdic.getString("Feb"),0);
		tblYearMonth.getRow(2).setElementAt(mdic.getString("Mar"),0);
		tblYearMonth.getRow(3).setElementAt(mdic.getString("Apr"),0);
		tblYearMonth.getRow(4).setElementAt(mdic.getString("May"),0);
		tblYearMonth.getRow(5).setElementAt(mdic.getString("Jun"),0);
		tblYearMonth.getRow(6).setElementAt(mdic.getString("Jul"),0);
		tblYearMonth.getRow(7).setElementAt(mdic.getString("Aug"),0);
		tblYearMonth.getRow(8).setElementAt(mdic.getString("Sep"),0);
		tblYearMonth.getRow(9).setElementAt(mdic.getString("Oct"),0);
		tblYearMonth.getRow(10).setElementAt(mdic.getString("Nov"),0);
		tblYearMonth.getRow(11).setElementAt(mdic.getString("Dec"),0);
		////////////////////////////////////////////////////////
		tblWeekDay.setColumnNameEx(mdic.getString("WeekDayTitle"),0);
		tblMonthDay.setColumnNameEx(mdic.getString("MonthDayTitle"),0);
		tblYearMonth.setColumnNameEx(mdic.getString("YearMonthTitle"),0);
		bdrInput.setTitle(mdic.getString("InputTitle"));
		pnlSchedule.setTitle(mdic.getString("ScheduleTitle"));
		////////////////////////////////////////////////////////
		setTitle(mdic.getString("Title"));
		onChangeScheduleType();
	}
	////////////////////////////////////////////////////////
	private void onChangeScheduleType()
	{
		int iSelected = cboScheduleType.getSelectedIndex();
		if(iSelected == 0) // Daily
			lblAdditionValueDesc.setText(mdic.getString("Day"));
		else if(iSelected == 1) // Weekly
			lblAdditionValueDesc.setText(mdic.getString("Week"));
		else if(iSelected == 2) // Monthly
			lblAdditionValueDesc.setText(mdic.getString("Month"));
		else if(iSelected == 3) // Yearly
			lblAdditionValueDesc.setText(mdic.getString("Year"));
		pnlInput.updateUI();
	}
	////////////////////////////////////////////////////////
	private void fillDetailValue()
	{
		////////////////////////////////////////////////////////
		int iSelected = tblSchedule.getSelectedRow();
		if(iSelected < 0)
		{
			clearDetailValue();
			return;
		}
		Vector vtRow = tblSchedule.getRow(iSelected);
		Dictionary dic = (Dictionary)vtRow.elementAt(4);
		////////////////////////////////////////////////////////
		String strScheduleType = dic.getString("ScheduleType");
		if(strScheduleType == null || strScheduleType.length() == 0)
			strScheduleType = "0";
		int iScheduleType = Integer.parseInt(strScheduleType);
		String strAdditionValue = dic.getString("AdditionValue");
		if(strAdditionValue == null || strAdditionValue.length() == 0)
			strAdditionValue = "1";
		int iAdditionValue = Integer.parseInt(strAdditionValue);
		String strWeekDay = StringUtil.nvl(dic.getString("WeekDay"),"");
		String strMonthDay = StringUtil.nvl(dic.getString("MonthDay"),"");
		String strYearMonth = StringUtil.nvl(dic.getString("YearMonth"),"");
		////////////////////////////////////////////////////////
		cboScheduleType.setSelectedIndex(iScheduleType);
		txtAdditionValue.setText(String.valueOf(iAdditionValue));
		txtExpectedDate.setText(dic.getString("ExpectedDate"));
		txtStartTime.setText(dic.getString("StartTime"));
		txtEndTime.setText(dic.getString("EndTime"));
		txtExecutionTime.setText(dic.getString("ExecutionTime"));
		////////////////////////////////////////////////////////
		if(strWeekDay.length() == 0)
			tblWeekDay.setAllValueEx(1,"TRUE");
		else
		{
			for(int iIndex = 0;iIndex < tblWeekDay.getRowCount();iIndex++)
			{
				if(strWeekDay.indexOf("," + tblWeekDay.getRow(iIndex).elementAt(2) + ",") >= 0)
					tblWeekDay.getRow(iIndex).setElementAt("TRUE",1);
				else
					tblWeekDay.getRow(iIndex).setElementAt("FALSE",1);
			}
		}
		////////////////////////////////////////////////////////
		if(strMonthDay.length() == 0)
			tblMonthDay.setAllValueEx(1,"TRUE");
		else
		{
			for(int iIndex = 0;iIndex < tblMonthDay.getRowCount();iIndex++)
			{
				if(strMonthDay.indexOf("," + tblMonthDay.getRow(iIndex).elementAt(2) + ",") >= 0)
					tblMonthDay.getRow(iIndex).setElementAt("TRUE",1);
				else
					tblMonthDay.getRow(iIndex).setElementAt("FALSE",1);
			}
		}
		////////////////////////////////////////////////////////
		if(strYearMonth.length() == 0)
			tblYearMonth.setAllValueEx(1,"TRUE");
		else
		{
			for(int iIndex = 0;iIndex < tblYearMonth.getRowCount();iIndex++)
			{
				if(strYearMonth.indexOf("," + tblYearMonth.getRow(iIndex).elementAt(2) + ",") >= 0)
					tblYearMonth.getRow(iIndex).setElementAt("TRUE",1);
				else
					tblYearMonth.getRow(iIndex).setElementAt("FALSE",1);
			}
		}
	}
	////////////////////////////////////////////////////////
	private void clearDetailValue()
	{
		cboScheduleType.setSelectedIndex(0);
		txtExecutionTime.setText("");
		txtAdditionValue.setText("");
		txtStartTime.setText("");
		txtEndTime.setText("");
		txtExpectedDate.setText("");
		tblWeekDay.setAllValueEx(1,"FALSE");
		tblMonthDay.setAllValueEx(1,"FALSE");
		tblYearMonth.setAllValueEx(1,"FALSE");
	}
	////////////////////////////////////////////////////////
	private void fillDefaultValue()
	{
		cboScheduleType.setSelectedIndex(0);
		txtExecutionTime.setText("");
		txtAdditionValue.setText("");
		txtStartTime.setText("");
		txtEndTime.setText("");
		txtExpectedDate.setText("");
		tblWeekDay.setAllValueEx(1,"TRUE");
		tblMonthDay.setAllValueEx(1,"TRUE");
		tblYearMonth.setAllValueEx(1,"TRUE");
	}
	////////////////////////////////////////////////////////
	// Implementation
	////////////////////////////////////////////////////////
	public boolean validateInput(int iOldAction,int iNewAction)
	{
		if(iOldAction == ACTION_NONE && (iNewAction == ACTION_MODIFY || iNewAction == ACTION_REMOVE))
		{
			if(tblSchedule.getSelectedRow() < 0)
				return false;
		}
		if((iOldAction == ACTION_ADD || iOldAction == ACTION_MODIFY) && iNewAction == ACTION_SAVE)
		{
			////////////////////////////////////////////////////////
			if(txtExpectedDate.getText().length() > 0 && !DateUtil.isDate(txtExpectedDate.getText(),Global.FORMAT_DATE))
			{
				MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00004",mdic.getString("ExpectedDate")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtExpectedDate.requestFocus();
				return false;
			}
			if(txtStartTime.getText().length() > 0 && !DateUtil.isDate(txtStartTime.getText(),Global.FORMAT_TIME))
			{
				MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00004",mdic.getString("StartTime")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtStartTime.requestFocus();
				return false;
			}
			if(txtEndTime.getText().length() > 0 && !DateUtil.isDate(txtEndTime.getText(),Global.FORMAT_TIME))
			{
				MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00004",mdic.getString("EndTime")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtEndTime.requestFocus();
				return false;
			}
			if(txtStartTime.getText().length() > 0 && txtEndTime.getText().length() > 0 &&
			   DateUtil.toDate(txtEndTime.getText(),Global.FORMAT_TIME).compareTo(DateUtil.toDate(txtStartTime.getText(),Global.FORMAT_TIME)) < 0)
			{
				MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00003",mdic.getString("EndTime"),mdic.getString("StartTime")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtEndTime.requestFocus();
				return false;
			}
			int iIndex = 0;
			while(iIndex < tblWeekDay.getRowCount() && !tblWeekDay.getRow(iIndex).elementAt(1).equals("TRUE"))
				iIndex++;
			if(iIndex >= tblWeekDay.getRowCount())
			{
				MessageBox.showMessageDialog(this,mdic.getString("WeekDayMessage"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				tblWeekDay.requestFocus();
				return false;
			}
			iIndex = 0;
			while(iIndex < tblYearMonth.getRowCount() && !tblYearMonth.getRow(iIndex).elementAt(1).equals("TRUE"))
				iIndex++;
			if(iIndex >= tblYearMonth.getRowCount())
			{
				MessageBox.showMessageDialog(this,mdic.getString("YearMonthMessage"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				tblYearMonth.requestFocus();
				return false;
			}
			iIndex = 0;
			while(iIndex < tblMonthDay.getRowCount() && !tblMonthDay.getRow(iIndex).elementAt(1).equals("TRUE"))
				iIndex++;
			if(iIndex >= tblMonthDay.getRowCount())
			{
				MessageBox.showMessageDialog(this,mdic.getString("MonthDayMessage"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				tblMonthDay.requestFocus();
				return false;
			}
			if(txtAdditionValue.getText().length() > 0 && txtAdditionValue.getText().equals("0"))
			{
				MessageBox.showMessageDialog(this,mdic.getString("AdditionValueMessage"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtAdditionValue.requestFocus();
				return false;
			}

			// Calculate execution time
			mdicSchedule = buildScheduleScript();
			Date dtNextExpectedDate = ScheduleUtil.calculateNextDate(mdicSchedule,false,0);
			if(dtNextExpectedDate == null)
			{
				MessageBox.showMessageDialog(this,mdic.getString("NeverRunMessage"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				tblYearMonth.requestFocus();
				return false;
			}
			if(MessageBox.showConfirmDialog(this,mdic.getString("NextDateMessage",StringUtil.format(dtNextExpectedDate,"EEEE dd MMMM, yyyy")),Global.APP_NAME,JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			{
				txtExpectedDate.setText(Global.FORMAT_DATE.format(dtNextExpectedDate));
				txtExpectedDate.getEditor().getEditorComponent().requestFocus();
				((JTextField)txtExpectedDate.getEditor().getEditorComponent()).select(0,txtExpectedDate.getText().length());
				return false;
			}
			DictionaryNode nd = mdicSchedule.getNode("ExpectedDate");
			nd.mstrValue = Global.FORMAT_DATE.format(dtNextExpectedDate);
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public void onChangeAction(int iOldAction,int iNewAction)
	{
		if(iNewAction == ACTION_NONE)
		{
			// Set control state
			cboScheduleType.setEnabled(false);
			txtExecutionTime.setEnabled(false);
			txtAdditionValue.setEnabled(false);
			txtStartTime.setEnabled(false);
			txtEndTime.setEnabled(false);
			txtExpectedDate.setEnabled(false);
			tblWeekDay.setEnabled(false);
			tblMonthDay.setEnabled(false);
			tblYearMonth.setEnabled(false);
			tblSchedule.setEnabled(true);

			// Default focus
			tblSchedule.requestFocus();

			// Fill detail value
			fillDetailValue();
		}
		else if(iNewAction == ACTION_ADD || iNewAction == ACTION_MODIFY || iNewAction == ACTION_SEARCH)
		{
			// Set control state
			cboScheduleType.setEnabled(true);
			txtExecutionTime.setEnabled(true);
			txtAdditionValue.setEnabled(true);
			txtStartTime.setEnabled(true);
			txtEndTime.setEnabled(true);
			txtExpectedDate.setEnabled(true);
			tblWeekDay.setEnabled(true);
			tblMonthDay.setEnabled(true);
			tblYearMonth.setEnabled(true);
			tblSchedule.setEnabled(false);

			if(iNewAction == ACTION_ADD)
				fillDefaultValue();
			else if(iNewAction == ACTION_SEARCH)
				clearDetailValue();
		}
	}
	////////////////////////////////////////////////////////
	public void backup()
	{
		cboScheduleType.backup();
		txtExecutionTime.backup();
		txtAdditionValue.backup();
		txtStartTime.backup();
		txtEndTime.backup();
		txtExpectedDate.backup();
		tblWeekDay.backup();
		tblMonthDay.backup();
		tblYearMonth.backup();
	}
	////////////////////////////////////////////////////////
	public void restore()
	{
		cboScheduleType.restore();
		txtExecutionTime.restore();
		txtAdditionValue.restore();
		txtStartTime.restore();
		txtEndTime.restore();
		txtExpectedDate.restore();
		tblWeekDay.restore();
		tblMonthDay.restore();
		tblYearMonth.restore();
	}
	////////////////////////////////////////////////////////
	public boolean isRestorable()
	{
		if(cboScheduleType.isChanged() ||
		   txtExecutionTime.isChanged() ||
		   txtAdditionValue.isChanged() ||
		   txtStartTime.isChanged() ||
		   txtEndTime.isChanged() ||
		   txtExpectedDate.isChanged() ||
		   tblWeekDay.isChanged() ||
		   tblMonthDay.isChanged() ||
		   tblYearMonth.isChanged())
			return true;
		return false;
	}
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		cboScheduleType.clearBackup();
		txtExecutionTime.clearBackup();
		txtAdditionValue.clearBackup();
		txtStartTime.clearBackup();
		txtEndTime.clearBackup();
		txtExpectedDate.clearBackup();
		tblWeekDay.clearBackup();
		tblMonthDay.clearBackup();
		tblYearMonth.clearBackup();
	}
	////////////////////////////////////////////////////////
	public Dictionary buildScheduleScript()
	{
		////////////////////////////////////////////////////////
		Dictionary dic = new Dictionary();
		dic.mndRoot.mvtChild = new Vector();
		////////////////////////////////////////////////////////
		dic.mndRoot.setChildValue("ScheduleType",String.valueOf(cboScheduleType.getSelectedIndex()));
		dic.mndRoot.setChildValue("ExecutionTime",txtExecutionTime.getText());
		dic.mndRoot.setChildValue("AdditionValue",txtAdditionValue.getText());
		dic.mndRoot.setChildValue("StartTime",txtStartTime.getText());
		dic.mndRoot.setChildValue("EndTime",txtEndTime.getText());
		dic.mndRoot.setChildValue("ExpectedDate",txtExpectedDate.getText());
		////////////////////////////////////////////////////////
		String str = "";
		boolean bAll = true;
		for(int iIndex = 0;iIndex < tblWeekDay.getRowCount();iIndex++)
		{
			if(tblWeekDay.getRow(iIndex).elementAt(1).equals("TRUE"))
				str += tblWeekDay.getRow(iIndex).elementAt(2) + ",";
			else
				bAll = false;
		}
		if(bAll)
			str = "";
		if(str.length() > 0)
			str = "," + str;
		dic.mndRoot.setChildValue("WeekDay",str);
		////////////////////////////////////////////////////////
		str = "";
		bAll = true;
		for(int iIndex = 0;iIndex < tblMonthDay.getRowCount();iIndex++)
		{
			if(tblMonthDay.getRow(iIndex).elementAt(1).equals("TRUE"))
				str += tblMonthDay.getRow(iIndex).elementAt(0) + ",";
			else
				bAll = false;
		}
		if(bAll)
			str = "";
		if(str.length() > 0)
			str = "," + str;
		dic.mndRoot.setChildValue("MonthDay",str);
		////////////////////////////////////////////////////////
		str = "";
		bAll = true;
		for(int iIndex = 0;iIndex < tblYearMonth.getRowCount();iIndex++)
		{
			if(tblYearMonth.getRow(iIndex).elementAt(1).equals("TRUE"))
				str += tblYearMonth.getRow(iIndex).elementAt(2) + ",";
			else
				bAll = false;
		}
		if(bAll)
			str = "";
		if(str.length() > 0)
			str = "," + str;
		dic.mndRoot.setChildValue("YearMonth",str);
		////////////////////////////////////////////////////////
		return dic;
	}
	////////////////////////////////////////////////////////
	public boolean add()
	{
		try
		{
			// Make script
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			mdicSchedule.store(os);

			// Call add
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("Schedule",new String(os.toByteArray()));
			DDTP response = channel.sendRequest("ThreadProcessor","addSchedule",request);
			String strScheduleID = response.getString("ScheduleID");

			// Update UI
			Vector vtRow = new Vector();
			vtRow.addElement(strScheduleID);
			vtRow.addElement(ScheduleUtil.getScheduleDescription(mdicSchedule));
			vtRow.addElement(Global.FORMAT_DATE.format(ScheduleUtil.getExpectedDate(mdicSchedule)));
			vtRow.addElement(String.valueOf(ScheduleUtil.getExecutionCount(mdicSchedule)));
			vtRow.addElement(mdicSchedule);
			tblSchedule.addRow(vtRow);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtExpectedDate.requestFocus();
			return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean modify()
	{
		try
		{
			int iSelected = tblSchedule.getSelectedRow();
			Vector vtRow = tblSchedule.getRow(iSelected);
			String strScheduleID = (String)vtRow.elementAt(0);

			// Make script
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			mdicSchedule.store(os);

			// Call update
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ScheduleID",strScheduleID);
			request.setString("Schedule",new String(os.toByteArray()));
			channel.sendRequest("ThreadProcessor","updateSchedule",request);

			// Update UI
			vtRow.setElementAt(ScheduleUtil.getScheduleDescription(mdicSchedule),1);
			vtRow.setElementAt(Global.FORMAT_DATE.format(ScheduleUtil.getExpectedDate(mdicSchedule)),2);
			vtRow.setElementAt(String.valueOf(ScheduleUtil.getExecutionCount(mdicSchedule)),3);
			vtRow.setElementAt(mdicSchedule,4);
			tblSchedule.setRow(iSelected,vtRow);
			if(tblSchedule.getRowCount() > 0)
			{
				if(iSelected < 0 || iSelected >= tblSchedule.getRowCount())
					iSelected = tblSchedule.getRowCount() - 1;
				tblSchedule.changeSelectedRow(iSelected);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtExpectedDate.requestFocus();
			return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean remove()
	{
		try
		{
			int iSelected = tblSchedule.getSelectedRow();
			Vector vtRow = tblSchedule.getRow(iSelected);
			String strScheduleID = (String)vtRow.elementAt(0);

			// Call delete
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ScheduleID",strScheduleID);
			channel.sendRequest("ThreadProcessor","deleteSchedule",request);

			// Update UI
			tblSchedule.deleteRow(iSelected);
			if(tblSchedule.getRowCount() > 0)
			{
				if(iSelected < 0 || iSelected >= tblSchedule.getRowCount())
					iSelected = tblSchedule.getRowCount() - 1;
				tblSchedule.changeSelectedRow(iSelected);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean search()
	{
		try
		{
			// Call search
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			DDTP response = channel.sendRequest("ThreadProcessor","querySchedule",request);
			String strSchedule = (String)response.getReturn();
			ByteArrayInputStream is = new ByteArrayInputStream(strSchedule.getBytes());
			Dictionary dic = new Dictionary(is);
			Vector vtSchedule = ScheduleUtil.scriptToSchedule(dic);

			// Update UI
			Vector vtData = new Vector();
			for(int iIndex = 0;iIndex < vtSchedule.size();iIndex++)
			{
				dic = (Dictionary)vtSchedule.elementAt(iIndex);
				Vector vtRow = new Vector();
				vtRow.addElement(dic.getString("ScheduleID"));
				vtRow.addElement(ScheduleUtil.getScheduleDescription(dic));
				vtRow.addElement(Global.FORMAT_DATE.format(ScheduleUtil.getExpectedDate(dic)));
				vtRow.addElement(String.valueOf(ScheduleUtil.getExecutionCount(dic)));
				vtRow.addElement(dic);
				vtData.addElement(vtRow);
			}
			tblSchedule.setData(vtData);
			if(tblSchedule.getRowCount() > 0)
			{
				tblSchedule.changeSelectedRow(0);
				fillDetailValue();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean print()
	{
		return true;
	}
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		if(pnlButton.exit())
			super.onCancel();
	}
	////////////////////////////////////////////////////////
	public void exit()
	{
		dispose();
	}
	////////////////////////////////////////////////////////
	public String getPermission()
	{
		return "SIUD";
	}
}
