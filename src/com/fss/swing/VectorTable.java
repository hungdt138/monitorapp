package com.fss.swing;

import java.io.*;
import java.sql.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.JComponent;

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

public class VectorTable extends JTable implements TrackChangeListener
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private static int DEFAULT_ROW_HEIGHT = -1;
	private static int DEFAULT_HEADER_HEIGHT = -1;
	public static int ALIGN_LEFT = JLabel.LEFT;
	public static int ALIGN_CENTER = JLabel.CENTER;
	public static int ALIGN_RIGHT = JLabel.RIGHT;
	////////////////////////////////////////////////////////
	// Nested class
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	// Dialog filter
	// Used to filter table data
	////////////////////////////////////////////////////////
	private class DialogFilter extends JXDialog
	{
		////////////////////////////////////////////////////////
		// Member variables
		////////////////////////////////////////////////////////
		private Vector mvtEditor = new Vector();
		private JButton btnOK = new JButton();
		private JButton btnCancel = new JButton();
		private int miReturn = JOptionPane.CANCEL_OPTION;
		////////////////////////////////////////////////////////
		public DialogFilter(Component cmpParent) throws Exception
		{
			super(cmpParent,true);
			setTitle(DefaultDictionary.getString("VectorTable.FilterTitle"));
			Container pnlMain = getContentPane();
			pnlMain.setLayout(new GridBagLayout());
			for(int iIndex = 0;iIndex < getColumnCount();iIndex++)
			{
				VectorTableColumn col = getColumn(iIndex);
				JLabel lbl = new JLabel(StringUtil.nvl(col.getHeaderValue(),""));
				JComponent cmp = prepareEditor(col,col.getFilterValue());
				pnlMain.add(lbl,new GridBagConstraints(0,iIndex,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
				pnlMain.add(cmp,new GridBagConstraints(1,iIndex,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
				mvtEditor.addElement(cmp);
			}
			////////////////////////////////////////////////////////
			JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
			pnlButton.add(btnOK);
			pnlButton.add(btnCancel);
			pnlMain.add(pnlButton,new GridBagConstraints(0,getColumnCount(),2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
			getRootPane().setDefaultButton(btnOK);
			////////////////////////////////////////////////////////
			// Event map
			////////////////////////////////////////////////////////
			btnOK.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onOK();
				}
			});
			////////////////////////////////////////////////////////
			btnCancel.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onCancel();
				}
			});
			////////////////////////////////////////////////////////
			Skin.applySkin(this);
			DefaultDictionary.applyButton(btnOK,"OK");
			DefaultDictionary.applyButton(btnCancel,"Cancel");
			////////////////////////////////////////////////////////
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.pack();
			int iWidth = this.getHeight() * 4 / 3;
			if(iWidth < 240)
				iWidth = 240;
			this.setSize(iWidth,this.getHeight());

		}
		////////////////////////////////////////////////////////
		/**
		 *
		 * @param col VectorTableColumn
		 * @param value String
		 * @return JComponent
		 */
		////////////////////////////////////////////////////////
		public JComponent prepareEditor(VectorTableColumn col,String value)
		{
			// Check enabled
			JComponent cmp = col.getEditorComponent();
			JComponent cmpReturn = null;

			// Set display value
			if(value == null)
				value = "";
			else if(col.getFormat() != null && !(value instanceof String))
				value = col.getFormat().format(value);
			if(cmp instanceof JToggleButton)
			{
				cmpReturn = new JXCombo();
				((JXCombo)cmpReturn).addItem("");
				((JXCombo)cmpReturn).addItem("TRUE");
				((JXCombo)cmpReturn).addItem("FALSE");
				((JXCombo)cmpReturn).setSelectedValue(value);
			}
			else if(cmp instanceof JXCombo)
			{
				cmpReturn = new JXCombo();
				VectorTable tblSrc = ((JXCombo)cmp).getPopupTable();
				VectorTable tblDes = ((JXCombo)cmpReturn).getPopupTable();
				((JXCombo)cmpReturn).fillValue(tblSrc.getData(),tblSrc.getDisplayIndex(),tblSrc.getValueIndex(),true);
				tblDes.removeAllColumn();
				for(int iIndex = 0;iIndex < tblSrc.getColumnCount();iIndex++)
				{
					VectorTableColumn colDes = tblSrc.getColumn(iIndex).duplicate();
					col.setParentTable(tblDes);
					tblDes.addColumn(colDes);
				}
				((JXCombo)cmpReturn).setSelectedValue(value);
			}
			else if(cmp instanceof JComboBox)
			{
				cmpReturn = new JXCombo();
				((JXCombo)cmpReturn).addItem("");
				for(int iIndex = 0;iIndex < ((JComboBox)cmp).getItemCount();iIndex++)
					((JXCombo)cmpReturn).addItem(((JComboBox)cmp).getItemAt(iIndex));
				((JXCombo)cmpReturn).setSelectedItem(value);
			}
			else
			{
				cmpReturn = new JXText();
				((JXText)cmpReturn).setText(value);
			}
			return cmpReturn;
		}
		////////////////////////////////////////////////////////
		// Event handling
		////////////////////////////////////////////////////////
		public void onOK()
		{
			try
			{
				// Fill filter value
				for(int iIndex = 0;iIndex < mvtEditor.size();iIndex++)
				{
					JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
					VectorTableColumn col = getColumn(iIndex);
					if(cmp instanceof JXText)
						col.setFilterValue(((JXText)cmp).getText());
					else if(cmp instanceof JComboBox)
					{
						if(cmp instanceof JXCombo)
						{
							Object obj = ((JXCombo)cmp).getSelectedValue();
							if(obj == null)
								col.setFilterValue(StringUtil.nvl(((JXCombo)cmp).getSelectedItem(),""));
							else
								col.setFilterValue(StringUtil.nvl(obj,""));
						}
						else if(col.getValueMap() != null)
						{
							int iSelected = ((JComboBox)cmp).getSelectedIndex();
							if(iSelected < 0)
								col.setFilterValue("");
							else
								col.setFilterValue((String)col.getValueMap().elementAt(iSelected));
							col.setFilterValue(StringUtil.nvl(((JXCombo)cmp).getSelectedValue(),""));
						}
						else
							col.setFilterValue(StringUtil.nvl(((JXCombo)cmp).getSelectedItem(),""));
					}
					else
						col.setFilterValue("");
				}
				miReturn = JOptionPane.OK_OPTION;
				dispose();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			}
		}
		////////////////////////////////////////////////////////
		public void onCancel()
		{
			boolean bChanged = false;
			int iIndex = 0;
			while(!bChanged && iIndex < mvtEditor.size())
			{
				Object obj = mvtEditor.elementAt(iIndex);
				if(obj instanceof TrackChangeListener &&
				   ((TrackChangeListener)obj).isChanged())
					bChanged = true;
				else
					iIndex++;
			}
			if(bChanged)
			{
				int iResult = MessageBox.showConfirmDialog(this,
					DefaultDictionary.getString("Confirm.SaveOnExit"),Global.APP_NAME,MessageBox.YES_NO_CANCEL_OPTION);
				if(iResult == MessageBox.CANCEL_OPTION)
					return;
				if(iResult == MessageBox.YES_OPTION)
					onOK();
				else
					dispose();
			}
			else
				dispose();
		}
	}
	////////////////////////////////////////////////////////
	// Dialog filter
	// Used to filter table data
	////////////////////////////////////////////////////////
	private class DialogExport extends JXDialog
	{
		////////////////////////////////////////////////////////
		// Member variables
		////////////////////////////////////////////////////////
		private JLabel lblFileName = new JLabel();
		private JLabel lblSeparator = new JLabel();
		private JXText txtFileName = new JXText();
		private JButton btnBrowse = new JButton("...");
		private JXText txtSeparator = new JXText();
		private JButton btnOK = new JButton();
		private JButton btnCancel = new JButton();
		////////////////////////////////////////////////////////
		public DialogExport(Component cmpParent) throws Exception
		{
			super(cmpParent,true);
			setTitle(DefaultDictionary.getString("VectorTable.ExportTitle"));
			lblFileName.setText(DefaultDictionary.getString("VectorTable.FileName"));
			lblSeparator.setText(DefaultDictionary.getString("VectorTable.Separator"));
			////////////////////////////////////////////////////////
			JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
			pnlButton.add(btnOK);
			pnlButton.add(btnCancel);
			getRootPane().setDefaultButton(btnOK);
			////////////////////////////////////////////////////////
			Container pnlMain = getContentPane();
			pnlMain.setLayout(new GridBagLayout());
			pnlMain.add(lblFileName,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
			pnlMain.add(txtFileName,new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
			pnlMain.add(btnBrowse,new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
			pnlMain.add(lblSeparator,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
			pnlMain.add(txtSeparator,new GridBagConstraints(1,2,2,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
			pnlMain.add(pnlButton,new GridBagConstraints(0,3,3,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
			btnBrowse.setMargin(new Insets(0,2,0,2));
			btnBrowse.setPreferredSize(new Dimension(btnBrowse.getPreferredSize().width,txtFileName.getPreferredSize().height));
			////////////////////////////////////////////////////////
			// Event map
			////////////////////////////////////////////////////////
			btnOK.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onOK();
				}
			});
			////////////////////////////////////////////////////////
			btnCancel.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onCancel();
				}
			});
			////////////////////////////////////////////////////////
			btnBrowse.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onBrowse();
				}
			});
			////////////////////////////////////////////////////////
			Skin.applySkin(this);
			DefaultDictionary.applyButton(btnOK,"OK");
			DefaultDictionary.applyButton(btnCancel,"Cancel");
			////////////////////////////////////////////////////////
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.pack();
			int iWidth = this.getHeight() * 4 / 3;
			if(iWidth < 240)
				iWidth = 240;
			this.setSize(iWidth,this.getHeight());
		}
		////////////////////////////////////////////////////////
		// Event handling
		////////////////////////////////////////////////////////
		public void onOK()
		{
			try
			{
				String strFileName = txtFileName.getText().trim();
				if(strFileName.length() == 0)
				{
					MessageBox.showMessageDialog(this,DefaultDictionary.getString("VectorTable.FileRequiredWarning"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
					txtFileName.requestFocus();
					return;
				}
				File fl = new File(txtFileName.getText());
				if(fl.getName().length() == 0)
				{
					MessageBox.showMessageDialog(this,DefaultDictionary.getString("VectorTable.FileRequiredWarning"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
					txtFileName.requestFocus();
					return;
				}
				if(fl.getName().indexOf(".") < 0)
					fl = new File(strFileName + ".txt");
				storeCvs(fl,StringEscapeUtil.unescapeJava(txtSeparator.getText()));
				dispose();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				txtFileName.requestFocus();
				MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			}
		}
		////////////////////////////////////////////////////////
		private void onBrowse()
		{
			JFileChooser fc = new JFileChooser();
			if(fc.showSaveDialog(this) == fc.APPROVE_OPTION)
				txtFileName.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}
	////////////////////////////////////////////////////////
	// VectorTableComparator
	// Used to sort table
	////////////////////////////////////////////////////////
	private class VectorTableComparator implements Comparator
	{
		////////////////////////////////////////////////////////
		private int miColumnModelIndex,miDataModelIndex,miSortType;
		private Format fmt;
		public VectorTableComparator(int iColumn,boolean blnSortAsc)
		{
			miColumnModelIndex = iColumn;
			fmt = getColumnFormatter(miColumnModelIndex);
			miDataModelIndex = getColumn(miColumnModelIndex).getModelIndex();
			if(blnSortAsc) miSortType = 1;
			else miSortType = -1;
		}
		////////////////////////////////////////////////////////
		public int compare(Object obj1,Object obj2)
		{
			try
			{
				obj1 = ((Vector)obj1).elementAt(miDataModelIndex);
				obj2 = ((Vector)obj2).elementAt(miDataModelIndex);
				if(obj1 == null && obj2 == null) return 0;
				if(obj1 == null) return 0 - miSortType;
				if(obj2 == null) return miSortType;
				if(obj1.equals(obj2)) return 0;
				if(obj1 instanceof Boolean)
				{
					if(obj1.equals(Boolean.TRUE)) return miSortType;
					return 0 - miSortType;
				}

				if(fmt != null)
				{
					if(obj1 instanceof String)
						obj1 = fmt.parseObject((String)obj1);
					if(obj2 instanceof String)
						obj2 = fmt.parseObject((String)obj2);
				}
				if(obj1 instanceof String && obj2 instanceof String)
					return miSortType * ((String)obj1).toLowerCase().compareTo(((String)obj2).toLowerCase());
				else if(obj1 instanceof Comparable)
					return miSortType * ((Comparable)obj1).compareTo((Comparable)obj2);
				return 0;
			}
			catch(Exception e)
			{
				return obj1.toString().compareTo(obj2.toString());
			}
		}
	}
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private TableRowHeader rowHeader = new TableRowHeader();
	private JViewport rowHeaderViewport = new JViewport();
	private Vector mvtTableData = new Vector();
	private Vector mvtSampleRow = null;
	private Vector mvtInvisibleColumn = new Vector();
	private Vector mvtShowMenu;
	private JPopupMenu mnuPopup = new JPopupMenu();
	////////////////////////////////////////////////////////
	private boolean allowFilter = true;
	private boolean allowExport = true;
	private boolean allowInsert = false;
	private boolean allowDelete = false;
	private boolean allowHide = false;
	private int iSortedColumn = -1;
	private boolean blnSortAsc = true;
	private int miRowSize;
	private boolean mbSorting = false;
	private Point mptLastRightClick;
	////////////////////////////////////////////////////////
	protected int miDisplayIndex = 0;
	protected int miValueIndex = 0;
	private boolean mbChildVisible = false;
	private MouseListener lsnPopup;
	////////////////////////////////////////////////////////
	public VectorTable(int iRowSize)
	{
		this.setAutoCreateColumnsFromModel(false);
		this.setRowSize(iRowSize);
		this.setModel(new VectorModel(this));
		this.defaultEditorsByColumnClass.clear();
		this.defaultRenderersByColumnClass.clear();
		this.setSurrendersFocusOnKeystroke(true);
		if(getDefaultRowHeight() > 0)
			setRowHeight(getDefaultRowHeight());
		if(getDefaultHeaderHeight() > 0)
			getTableHeader().setPreferredSize(new Dimension(getTableHeader().getPreferredSize().width,getDefaultHeaderHeight()));
		putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
		rowHeaderViewport.setView(rowHeader);
		rowHeaderViewport.setPreferredSize(new Dimension(0,0));
		setAllowDelete(false);
		setAllowInsert(false);
		setAllowFilter(true);
		setAllowExport(true);
		setAllowHideColumn(true);
		////////////////////////////////////////////////////////
		lsnPopup = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(isEnabled())
				{
					if(e.getButton() == e.BUTTON3)
						showPopup(e);
				}
			}
		};
		addMouseListener(lsnPopup);
		////////////////////////////////////////////////////////
		rowHeader.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting())
					changeSelectedRow(rowHeader.getSelectedRow());
			}
		});
		////////////////////////////////////////////////////////
		getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting())
					rowHeader.changeSelection(getSelectedRow(),rowHeader.getSelectedColumn(),false,false);
			}
		});
		////////////////////////////////////////////////////////
		this.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent evt)
			{
				onKeyPressed(evt);
			}
			public void keyTyped(KeyEvent evt){}
			public void keyReleased(KeyEvent evt){}
		});
		getTableHeader().addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if(isEnabled())
				{
					if(evt.getButton() == evt.BUTTON3)
						showPopup(evt);
					else
						onHeaderClicked(evt);
				}
			}
			public void mousePressed(MouseEvent evt){}
			public void mouseReleased(MouseEvent evt){}
			public void mouseExited(MouseEvent evt){}
			public void mouseEntered(MouseEvent evt){}
		});
	}
	////////////////////////////////////////////////////////
	public boolean isHeaderSort(int iColIndex)
	{
		return getColumn(iColIndex).isHeaderSort();
	}
	////////////////////////////////////////////////////////
	public boolean isHeaderSortEx(int iModelIndex)
	{
		return getColumnEx(iModelIndex).isHeaderSort();
	}
	////////////////////////////////////////////////////////
	public void setHeaderSort(int iColIndex,boolean bHeaderSort)
	{
		getColumn(iColIndex).setHeaderSort(bHeaderSort);
	}
	////////////////////////////////////////////////////////
	public void setHeaderSortEx(int iModelIndex,boolean bHeaderSort)
	{
		getColumnEx(iModelIndex).setHeaderSort(bHeaderSort);
	}
	////////////////////////////////////////////////////////
	public void setHeaderSort(boolean bHeaderSort)
	{
		for(int iIndex = 0;iIndex < getColumnCount();iIndex++)
			setHeaderSort(iIndex,bHeaderSort);
	}
	////////////////////////////////////////////////////////
	public void setDisplayIndex(int iIndex)
	{
		if(iIndex < getRowSize() && iIndex >= 0)
			miDisplayIndex = iIndex;
		else
			throw new IllegalArgumentException();
	}
	////////////////////////////////////////////////////////
	public int getDisplayIndex()
	{
		return miDisplayIndex;
	}
	////////////////////////////////////////////////////////
	public void setValueIndex(int iIndex)
	{
		if(iIndex < getRowSize() && iIndex >= 0)
			miValueIndex = iIndex;
		else
			throw new IllegalArgumentException();
	}
	////////////////////////////////////////////////////////
	public int getValueIndex()
	{
		return miValueIndex;
	}
	////////////////////////////////////////////////////////
	public int getRowIndexForValueData(Object obj)
	{
		for(int iIndex = 0;iIndex < mvtFilteredData.size();iIndex++)
		{
			if(getRow(iIndex).elementAt(miValueIndex).equals(obj))
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	public int getRowIndexForDisplayData(Object obj)
	{
		for(int iIndex = 0;iIndex < mvtFilteredData.size();iIndex++)
		{
			if(getRow(iIndex).elementAt(miDisplayIndex).equals(obj))
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	public void setAllowInsert(boolean bAllowInsert)
	{
		this.allowInsert = bAllowInsert;
		if(allowInsert)
		{
			if(mvtSampleRow == null)
			{
				mvtSampleRow = new Vector();
				correctData(mvtSampleRow);
				((VectorModel)dataModel).fireTableRowsInserted(mvtFilteredData.size(),mvtFilteredData.size());
				((VectorModel)dataModel).fireIntervalAdded(this,mvtFilteredData.size(),mvtFilteredData.size());
			}
		}
		else
			mvtSampleRow = null;
	}
	////////////////////////////////////////////////////////
	public boolean isAllowInsert()
	{
		return allowInsert;
	}
	////////////////////////////////////////////////////////
	public boolean isAllowHideColumn()
	{
		return allowHide;
	}
	////////////////////////////////////////////////////////
	public void setAllowHideColumn(boolean allowHide)
	{
		this.allowHide = allowHide;
	}
	////////////////////////////////////////////////////////
	public void setAllowDelete(boolean allowDelete)
	{
		this.allowDelete = allowDelete;
	}
	////////////////////////////////////////////////////////
	public boolean isAllowDelete()
	{
		return allowDelete;
	}
	////////////////////////////////////////////////////////
	public void setAllowFilter(boolean allowFilter)
	{
		if(!allowFilter)
		{
			if(this.allowFilter != allowFilter)
			{
				mvtFilteredData = (Vector)mvtTableData.clone();
				int iSelectedColumn[] = getColumnModel().getSelectedColumns();
				((VectorModel)dataModel).fireTableDataChanged();
				((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
				for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
					getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);
			}
		}
		this.allowFilter = allowFilter;
	}
	////////////////////////////////////////////////////////
	public boolean isAllowFilter()
	{
		return allowFilter;
	}
	////////////////////////////////////////////////////////
	public void setAllowExport(boolean allowExport)
	{
		this.allowExport = allowExport;
	}
	////////////////////////////////////////////////////////
	public boolean isAllowExport()
	{
		return allowExport;
	}
	////////////////////////////////////////////////////////
	// Event handling
	////////////////////////////////////////////////////////
	private void showPopup(MouseEvent e)
	{
		TableCellEditor editor = getCellEditor();
		if(editor != null)
			editor.stopCellEditing();
		int iRowIndex = rowAtPoint(e.getPoint());
		boolean bRowFound = true;
		if(getRowSelectionAllowed())
		{
			int iIndex = 0;
			int iSelectedRows[] = getSelectedRows();
			bRowFound = false;
			while(!bRowFound && iIndex < iSelectedRows.length)
			{
				if(iSelectedRows[iIndex] == iRowIndex)
					bRowFound = true;
				else
					iIndex++;
			}
		}
		int iColIndex = columnAtPoint(e.getPoint());
		boolean bColFound = true;
		if(getColumnSelectionAllowed())
		{
			int iIndex = 0;
			int iSelectedCols[] = getSelectedColumns();
			bColFound = false;
			while(!bColFound && iIndex < iSelectedCols.length)
			{
				if(iSelectedCols[iIndex] == iColIndex)
					bColFound = true;
				else
					iIndex++;
			}
		}
		if(!bRowFound || !bColFound)
			changeSelection(iRowIndex,iColIndex,false,false);
		mptLastRightClick = e.getPoint();
		try
		{
			mbChildVisible = true;
			preparePopupMenu();
			mnuPopup.show((Component)e.getSource(),e.getX(),e.getY());
		}
		finally
		{
			mbChildVisible = false;
		}
	}
	////////////////////////////////////////////////////////
	private void hideColumn()
	{
		if(mptLastRightClick != null)
			hideColumn(columnAtPoint(mptLastRightClick));
	}
	////////////////////////////////////////////////////////
	public void filter() throws Exception
	{
		int iSelected[] = getSelectedRows();
		Vector vtSelected = new Vector();
		for(int iIndex = 0;iIndex < iSelected.length;iIndex++)
			vtSelected.addElement(getRow(iSelected[iIndex]));
		int iFilterIndex[] = new int[getColumnCount()];
		boolean bFilterLikeIndex[] = new boolean[getColumnCount()];
		Vector vtValue = new Vector();
		for(int iIndex = 0;iIndex < getColumnCount();iIndex++)
		{
			VectorTableColumn col = getColumn(iIndex);
			String strPattern = StringUtil.nvl(col.getFilterValue(),"");
			if(strPattern.length() > 0)
			{
				iFilterIndex[vtValue.size()] = col.getModelIndex();
				bFilterLikeIndex[vtValue.size()] = !(col.getEditorComponent() instanceof JXCombo ||
													 col.getEditorComponent() instanceof JToggleButton);
				if(bFilterLikeIndex[vtValue.size()])
				{
					strPattern = StringUtil.replaceAll(StringUtil.replaceAll(strPattern,"%","*"),"_","?");
					if(!WildcardFilter.isOptional(strPattern))
						vtValue.addElement(strPattern);
				}
				else
					vtValue.addElement(strPattern);
			}
		}

		if(vtValue.size() <= 0)
		{
			mvtFilteredData = (Vector)mvtTableData.clone();
			int iSelectedColumn[] = getColumnModel().getSelectedColumns();
			((VectorModel)dataModel).fireTableDataChanged();
			((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
			for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
				getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);
			return;
		}

		int iRowIndex = 0;
		mvtFilteredData.clear();
		while(iRowIndex < mvtTableData.size())
		{
			Vector vtRow = (Vector)mvtTableData.elementAt(iRowIndex);
			boolean bFound = false;
			for(int iIndex = 0;!bFound && iIndex < vtValue.size();iIndex++)
			{
				String strPattern = (String)vtValue.elementAt(iIndex);
				String strValue = StringUtil.nvl(vtRow.elementAt(iFilterIndex[iIndex]),"");
				if(bFilterLikeIndex[iIndex])
				{
					if(!WildcardFilter.match(strPattern,strValue))
						bFound = true;
				}
				else
				{
					if(!strValue.equals(strPattern))
						bFound = true;
				}
			}
			if(!bFound)
				mvtFilteredData.addElement(vtRow);
			iRowIndex++;
		}
		if(iSortedColumn >= 0)
			sort(iSortedColumn,blnSortAsc);
		else
		{
			int iSelectedColumn[] = getColumnModel().getSelectedColumns();
			((VectorModel)dataModel).fireTableDataChanged();
			((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
			for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
				getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);
		}
		getSelectionModel().clearSelection();
		boolean bScrolled = false;
		for(int iIndex = 0;iIndex < getRowCount();iIndex++)
		{
			if(vtSelected.indexOf(getRow(iIndex)) >= 0)
			{
				if(!bScrolled)
				{
					changeSelectedRow(iIndex);
					bScrolled = true;
				}
				else
					addRowSelectionInterval(iIndex,iIndex);
			}
		}
		if(getRowCount() > 0 && getSelectedRow() < 0)
			changeSelectedRow(0);
	}
	////////////////////////////////////////////////////////
	private void requestFilterCriteria()
	{
		try
		{
			mbChildVisible = true;
			DialogFilter dlg = new DialogFilter(this);
			WindowManager.centeredWindow(dlg);
			if(dlg.miReturn == JOptionPane.OK_OPTION)
				filter();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
		finally
		{
			mbChildVisible = false;
		}
	}
	////////////////////////////////////////////////////////
	private void export()
	{
		try
		{
			mbChildVisible = true;
			DialogExport dlg = new DialogExport(this);
			WindowManager.centeredWindow(dlg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
		finally
		{
			mbChildVisible = false;
		}
	}
	////////////////////////////////////////////////////////
	private void onHeaderClicked(MouseEvent evt)
	{
		int iColumn = this.getColumnModel().getColumnIndexAtX(evt.getX());
		if(isHeaderSort(iColumn) && isEnabled())
		{
			getColumnModel().getSelectionModel().setSelectionInterval(iColumn,iColumn);
			if(iSortedColumn == iColumn)
				blnSortAsc = !blnSortAsc;
			else
				iSortedColumn = iColumn;
			sort(iSortedColumn,blnSortAsc);
		}
	}
	////////////////////////////////////////////////////////
	private void onKeyPressed(KeyEvent evt)
	{
		int iKeyCode = evt.getKeyCode();
		int iRowIndex = getSelectedRow();
		if(iKeyCode == KeyEvent.VK_INSERT)
		{
			if(isAllowInsert())
			{
				if(iRowIndex >= 0 && iRowIndex < mvtFilteredData.size())
					insertRow(iRowIndex);
				evt.consume();
			}
		}
		else if(iKeyCode == KeyEvent.VK_DELETE)
		{
			if(this.allowDelete)
			{
				if(iRowIndex >= 0 && iRowIndex < mvtFilteredData.size())
					deleteRow(iRowIndex);
				evt.consume();
			}
		}
		else if(iKeyCode == KeyEvent.VK_ENTER)
		{
			editNextColumn();
			evt.consume();
		}
		else if(iKeyCode == KeyEvent.VK_TAB)
		{
			if(evt.isShiftDown())
				transferFocusBackward();
			else
				transferFocus();
			evt.consume();
		}
	}
	////////////////////////////////////////////////////////
	// Attributes
	////////////////////////////////////////////////////////
	public TableRowHeader getRowHeader()
	{
		return rowHeader;
	}
	////////////////////////////////////////////////////////
	public JTableHeader getCornerHeader()
	{
		return rowHeader.getTableHeader();
	}
	////////////////////////////////////////////////////////
	public JViewport getRowHeaderViewport()
	{
		return rowHeaderViewport;
	}
	////////////////////////////////////////////////////////
	// Column
	////////////////////////////////////////////////////////
	public int getRowSize()
	{
		return miRowSize;
	}
	////////////////////////////////////////////////////////
	public void setRowSize(int iRowSize)
	{
		miRowSize = iRowSize;
	}
	////////////////////////////////////////////////////////
	// Based on column index
	////////////////////////////////////////////////////////
	public String getColumnName(int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		return col.getHeaderValue().toString();
	}
	////////////////////////////////////////////////////////
	public Format getColumnFormatter(int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		return col.getFormat();
	}
	////////////////////////////////////////////////////////
	public JComponent getColumnEditor(int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		return col.getEditorComponent();
	}
	////////////////////////////////////////////////////////
	public Vector getColumnValueMap(int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		return col.getValueMap();
	}
	////////////////////////////////////////////////////////
	public void setColumnName(String strColName,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setHeaderValue(strColName);
		getTableHeader().repaint();
	}
	////////////////////////////////////////////////////////
	public void setColumnEditable(boolean bEditable,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setEditable(bEditable);
	}
	////////////////////////////////////////////////////////
	public void setColumnEditor(JComponent cmpEditor,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setEditorComponent(cmpEditor);
	}
	////////////////////////////////////////////////////////
	public void setColumnValueMap(Vector vctValue,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setValueMap(vctValue);
	}
	////////////////////////////////////////////////////////
	public void setColumnDefaultValue(String strDefaultValue,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setDefaultValue(strDefaultValue);
	}
	////////////////////////////////////////////////////////
	public void setColumnAlign(int iAlign,int iColIndex)
	{
		VectorTableColumn col = getColumn(iColIndex);
		col.setAlignment(iAlign);
		getTableHeader().repaint();
	}
	////////////////////////////////////////////////////////
	// Based on model index
	////////////////////////////////////////////////////////
	public String getColumnNameEx(int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		return col.getHeaderValue().toString();
	}
	////////////////////////////////////////////////////////
	public Format getColumnFormatterEx(int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		return col.getFormat();
	}
	////////////////////////////////////////////////////////
	public JComponent getColumnEditorEx(int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		return col.getEditorComponent();
	}
	////////////////////////////////////////////////////////
	public Vector getColumnValueMapEx(int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		return col.getValueMap();
	}
	////////////////////////////////////////////////////////
	public void setColumnNameEx(String strColName,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setHeaderValue(strColName);
		getTableHeader().repaint();
	}
	////////////////////////////////////////////////////////
	public void setColumnEditableEx(boolean bEditable,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setEditable(bEditable);
	}
	////////////////////////////////////////////////////////
	public void setColumnEditorEx(JComponent cmpEditor,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setEditorComponent(cmpEditor);
	}
	////////////////////////////////////////////////////////
	public void setColumnValueMapEx(Vector vctValue,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setValueMap(vctValue);
	}
	////////////////////////////////////////////////////////
	public void setColumnDefaultValueEx(String strDefaultValue,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setDefaultValue(strDefaultValue);
	}
	////////////////////////////////////////////////////////
	public void setColumnAlignEx(int iAlign,int iModelIndex)
	{
		VectorTableColumn col = getColumnEx(iModelIndex);
		col.setAlignment(iAlign);
		getTableHeader().repaint();
	}
	////////////////////////////////////////////////////////
	// Column processing
	////////////////////////////////////////////////////////
	public void addRowHeader(String strName,int iModelIndex,int iWidth)
	{
		rowHeader.addColumn(strName,iModelIndex,iWidth);
		Dimension dmViewPort = rowHeaderViewport.getPreferredSize();
		dmViewPort.width = dmViewPort.width + iWidth;
		dmViewPort.height = rowHeader.getPreferredSize().height;
		rowHeaderViewport.setPreferredSize(dmViewPort);
	}
	////////////////////////////////////////////////////////
	public void addColumn(String strName,int iModelIndex,boolean bEditable)
	{
		VectorTableColumn col = new VectorTableColumn(this);
		col.setHeaderValue(strName);
		col.setModelIndex(iModelIndex);
		col.setEditable(bEditable);
		getColumnModel().addColumn(col);
	}
	////////////////////////////////////////////////////////
	public void addColumn(String strName,int iModelIndex,boolean bEditable,Format fmt)
	{
		VectorTableColumn col = new VectorTableColumn(this);
		col.setHeaderValue(strName);
		col.setModelIndex(iModelIndex);
		col.setEditable(bEditable);
		col.setFormat(fmt);
		getColumnModel().addColumn(col);
	}
	////////////////////////////////////////////////////////
	public void showColumn(TableColumn col)
	{
		getColumnModel().addColumn(col);
		int iResult = mvtInvisibleColumn.indexOf(col);
		if(iResult >= 0)
			mvtInvisibleColumn.removeElementAt(iResult);
	}
	////////////////////////////////////////////////////////
	public void showColumn(int iColumnIndex)
	{
		addColumn((TableColumn)mvtInvisibleColumn.elementAt(iColumnIndex));
		mvtInvisibleColumn.removeElementAt(iColumnIndex);
	}
	////////////////////////////////////////////////////////
	public void showColumnEx(int iColumnIndex)
	{
		for(int iIndex = 0;iIndex < mvtInvisibleColumn.size();iIndex++)
		{
			TableColumn col = (TableColumn)mvtInvisibleColumn.elementAt(iIndex);
			if(col.getModelIndex() == iColumnIndex)
			{
				showColumn(iIndex);
				return;
			}
		}
	}
	////////////////////////////////////////////////////////
	public void hideColumn(TableColumn col)
	{
		if(isAllowHideColumn())
		{
			super.removeColumn(col);
			if(mvtInvisibleColumn.indexOf(col) < 0)
				mvtInvisibleColumn.addElement(col);
		}
	}
	////////////////////////////////////////////////////////
	public void hideColumn(int iColumnIndex)
	{
		TableColumn col = getColumn(iColumnIndex);
		hideColumn(col);
	}
	////////////////////////////////////////////////////////
	public void hideColumnEx(int iColumnIndex)
	{
		TableColumn col = getColumnEx(iColumnIndex);
		hideColumn(col);
	}
	////////////////////////////////////////////////////////
	public void removeColumn(TableColumn col)
	{
		super.removeColumn(col);
		if(mvtInvisibleColumn.indexOf(col) >= 0)
			mvtInvisibleColumn.removeElement(col);
	}
	////////////////////////////////////////////////////////
	public void removeAllColumn()
	{
		for(int iIndex = 0;iIndex < columnModel.getColumnCount();iIndex++)
			removeColumn((VectorTableColumn)columnModel.getColumn(iIndex));
		mvtInvisibleColumn.clear();
	}
	////////////////////////////////////////////////////////
	public int getColumnIndex(int iModelIndex)
	{
		for(int iColIndex = 0;iColIndex < columnModel.getColumnCount();iColIndex++)
		{
			if(columnModel.getColumn(iColIndex).getModelIndex() == iModelIndex)
				return iColIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * @deprecated Change to getColumnIndex
	 * @param iModelIndex int
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getColumnIndexByModelIndex(int iModelIndex)
	{
		return getColumnIndex(iModelIndex);
	}
	////////////////////////////////////////////////////////
	public VectorTableColumn getColumn(int iColIndex)
	{
		return (VectorTableColumn)columnModel.getColumn(iColIndex);
	}
	////////////////////////////////////////////////////////
	public VectorTableColumn getColumnEx(int iModelIndex)
	{
		int iColIndex = getColumnIndex(iModelIndex);
		if(iColIndex >= 0)
			return (VectorTableColumn)columnModel.getColumn(iColIndex);
		return null;
	}
	////////////////////////////////////////////////////////
	// Row processing
	////////////////////////////////////////////////////////
	public Vector getRow(int iRowIndex)
	{
		if(isAllowInsert() && iRowIndex == mvtFilteredData.size())
			return mvtSampleRow;
		return (Vector)mvtFilteredData.elementAt(iRowIndex);
	}
	////////////////////////////////////////////////////////
	public void setRow(int iRowIndex,Vector vtRowData)
	{
		Object obj = mvtFilteredData.elementAt(iRowIndex);
		mvtTableData.removeElement(obj);
		mvtTableData.insertElementAt(vtRowData,iRowIndex);
		mvtFilteredData.setElementAt(vtRowData,iRowIndex);
		correctData(iRowIndex);
		((VectorModel)dataModel).fireTableRowsUpdated(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireContentsChanged(this,iRowIndex,iRowIndex);
	}
	////////////////////////////////////////////////////////
	public int addRow()
	{
		Vector vtRowData = new Vector();
		int iRowIndex = mvtFilteredData.size();
		mvtFilteredData.addElement(vtRowData);
		mvtTableData.addElement(vtRowData);
		correctData(iRowIndex);
		((VectorModel)dataModel).fireTableRowsInserted(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireIntervalAdded(this,iRowIndex,iRowIndex);
		if(getSelectedRow() < 0)
			changeSelectedRow(mvtFilteredData.size() - 1);
		return iRowIndex;
	}
	////////////////////////////////////////////////////////
	public int addRow(Vector vtRowData)
	{
		int iRowIndex = mvtFilteredData.size();
		mvtFilteredData.addElement(vtRowData);
		mvtTableData.addElement(vtRowData);
		correctData(iRowIndex);
		((VectorModel)dataModel).fireTableRowsInserted(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireIntervalAdded(this,iRowIndex,iRowIndex);
		if(getSelectedRow() < 0)
			changeSelectedRow(mvtFilteredData.size() - 1);
		return iRowIndex;
	}
	////////////////////////////////////////////////////////
	public void insertRow(int iRowIndex,Vector vtRowData)
	{
		mvtFilteredData.insertElementAt(vtRowData,iRowIndex);
		mvtTableData.insertElementAt(vtRowData,iRowIndex);
		correctData(iRowIndex);
		((VectorModel)dataModel).fireTableRowsInserted(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireIntervalAdded(this,iRowIndex,iRowIndex);
		if(getSelectedRow() < 0)
		{
			if(iRowIndex >= mvtFilteredData.size())
				changeSelectedRow(mvtFilteredData.size() - 1);
			else if(iRowIndex > 0)
				changeSelectedRow(iRowIndex);
			else
				changeSelectedRow(0);
		}
	}
	////////////////////////////////////////////////////////
	public void insertRow(int iRowIndex)
	{
		Vector vtRowData = new Vector();
		mvtFilteredData.insertElementAt(vtRowData,iRowIndex);
		mvtTableData.insertElementAt(vtRowData,iRowIndex);
		correctData(iRowIndex);
		((VectorModel)dataModel).fireTableRowsInserted(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireIntervalAdded(this,iRowIndex,iRowIndex);
		if(getSelectedRow() < 0)
		{
			if(iRowIndex >= mvtFilteredData.size())
				changeSelectedRow(mvtFilteredData.size() - 1);
			else if(iRowIndex > 0)
				changeSelectedRow(iRowIndex);
			else
				changeSelectedRow(0);
		}
	}
	////////////////////////////////////////////////////////
	public void deleteRow(int iRowIndex)
	{
		Object obj = mvtFilteredData.elementAt(iRowIndex);
		mvtTableData.removeElement(obj);
		mvtFilteredData.removeElementAt(iRowIndex);
		((VectorModel)dataModel).fireTableRowsDeleted(iRowIndex,iRowIndex);
		((VectorModel)dataModel).fireIntervalRemoved(this,iRowIndex,iRowIndex);
		if(getRowCount() > 0 && getSelectedRow() < 0)
		{
			if(iRowIndex >= getRowCount())
				changeSelectedRow(getRowCount() - 1);
			else if(iRowIndex > 0)
				changeSelectedRow(iRowIndex);
			else
				changeSelectedRow(0);
		}
	}
	////////////////////////////////////////////////////////
	public void deleteRow(Vector vtRow)
	{
		int iIndex = mvtFilteredData.indexOf(vtRow);
		if(iIndex >= 0)
			deleteRow(iIndex);
		else
			mvtTableData.removeElement(vtRow);
	}
	////////////////////////////////////////////////////////
	public void deleteAllRow()
	{
		int iRowCount = mvtTableData.size();
		mvtTableData.clear();
		mvtFilteredData.clear();
		((VectorModel)dataModel).fireTableRowsDeleted(0,iRowCount);
		((VectorModel)dataModel).fireIntervalRemoved(this,0,iRowCount);
	}
	////////////////////////////////////////////////////////
	// Data processing
	////////////////////////////////////////////////////////
	public void correctData(Vector vtRowData)
	{
		int iColCount = dataModel.getColumnCount();
		while(vtRowData.size() < iColCount)
		{
			VectorTableColumn col = getColumnEx(vtRowData.size());
			if(col == null)
				vtRowData.addElement("");
			else
				vtRowData.addElement(col.getDefaultValue());
		}

		for(int iColIndex = iColCount;iColIndex < vtRowData.size();iColIndex++)
			if(vtRowData.elementAt(iColIndex) == null)
				vtRowData.setElementAt("",iColIndex);

		iColCount = columnModel.getColumnCount();
		for(int iColIndex = 0;iColIndex < iColCount;iColIndex++)
		{
			VectorTableColumn col = getColumn(iColIndex);
			Object objValue = vtRowData.elementAt(col.getModelIndex());
			if(objValue == null)
				vtRowData.setElementAt("",col.getModelIndex());
			else if(!(objValue instanceof String))
			{
				Format fmt = col.getFormat();
				if(fmt != null && !(objValue instanceof String))
					objValue = fmt.format(objValue);
				vtRowData.setElementAt(objValue,col.getModelIndex());
			}
		}
		vtRowData.trimToSize();
	}
	////////////////////////////////////////////////////////
	public void correctData(int iRowIndex)
	{
		Vector vtRowData = null;
		if(iRowIndex == mvtFilteredData.size())
			vtRowData = mvtSampleRow;
		else
			vtRowData = (Vector)mvtFilteredData.elementAt(iRowIndex);
		correctData(vtRowData);
	}
	////////////////////////////////////////////////////////
	public void correctData()
	{
		// Correct data
		int iRowCount = mvtFilteredData.size();
		for(int iRowIndex = 0;iRowIndex < iRowCount;iRowIndex++)
			correctData(iRowIndex);
	}
	////////////////////////////////////////////////////////
	public Vector getData()
	{
		if(isEditing())
			getCellEditor().stopCellEditing();
		return mvtTableData;
	}
	////////////////////////////////////////////////////////
	protected void afterSetData()
	{
		correctData();
		if(iSortedColumn >= 0 && this.isHeaderSort(iSortedColumn))
			sort(iSortedColumn,blnSortAsc);
		else
		{
			int iSelectedColumn[] = getColumnModel().getSelectedColumns();
			((VectorModel)dataModel).fireTableDataChanged();
			((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
			for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
				getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);

			if(getRowCount() > 0 && getSelectedRow() < 0)
				changeSelectedRow(0);
		}
	}
	////////////////////////////////////////////////////////
	public void setData(Vector vtData)
	{
		mvtTableData = vtData;
		mvtFilteredData = (Vector)mvtTableData.clone();
		afterSetData();
	}
	////////////////////////////////////////////////////////
	public void setData(String[][] strData)
	{
		int iRowNumber = strData.length;
		int iColNumber = getColumnCount();

		Vector vtTableData = new Vector();
		for(int iRowIndex = 0;iRowIndex < iRowNumber;iRowIndex++)
		{
			Vector vtRowData = new Vector();
			for(int iColIndex = 0;iColIndex < iColNumber;iColIndex++)
				vtRowData.addElement(strData[iRowIndex][iColIndex]);
			vtTableData.addElement(vtRowData);
		}
		mvtTableData = vtTableData;
		mvtFilteredData = (Vector)mvtTableData.clone();
		afterSetData();
	}
	////////////////////////////////////////////////////////
	public void setData(ResultSet rsData,boolean blnResetColumn) throws SQLException
	{
		int iColNumber;
		ResultSetMetaData mtData = rsData.getMetaData();
		iColNumber = mtData.getColumnCount();
		if(blnResetColumn)
		{
			createDefaultColumnModel();
			for(int iColIndex = 1;iColIndex <= iColNumber;iColIndex++)
				addColumn(mtData.getColumnLabel(iColIndex),iColIndex - 1,false);
		}
		if(iColNumber > getColumnCount())
			iColNumber = getColumnCount();

		mvtTableData = new Vector();
		while(rsData.next())
		{
			Vector vtRowData = new Vector();
			for(int iColIndex = 1;iColIndex <= iColNumber;iColIndex++)
				vtRowData.addElement(rsData.getObject(iColIndex));
			mvtTableData.addElement(vtRowData);
		}
		mvtFilteredData = (Vector)mvtTableData.clone();
		afterSetData();
	}
	////////////////////////////////////////////////////////
	public void setData(Hashtable prtData)
	{
		Object[] objValueList = prtData.values().toArray();
		Object[] objKeyList = prtData.keySet().toArray();

		Vector vtTableData = new Vector();
		int iCount = objKeyList.length;
		for(int iIndex = 0;iIndex < iCount;iIndex++)
		{
			Vector vtRowData = new Vector();
			vtRowData.addElement(objKeyList[iIndex]);
			vtRowData.addElement(objValueList[iIndex]);
			vtTableData.addElement(vtRowData);
		}
		mvtTableData = vtTableData;
		mvtFilteredData = (Vector)mvtTableData.clone();
		afterSetData();
	}
	////////////////////////////////////////////////////////
	public void appendData(Vector vtData)
	{
		int iFirstRow = mvtTableData.size();
		int iNumber = vtData.size();
		for(int iIndex = 0;iIndex < iNumber;iIndex++)
		{
			mvtTableData.addElement(vtData.elementAt(iIndex));
			mvtFilteredData.addElement(vtData.elementAt(iIndex));
			correctData(mvtFilteredData.size() - 1);
		}
		((VectorModel)dataModel).fireTableRowsInserted(iFirstRow,iFirstRow + iNumber - 1);
		((VectorModel)dataModel).fireIntervalAdded(this,iFirstRow,iFirstRow + iNumber - 1);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return JPopupMenu
	 */
	////////////////////////////////////////////////////////
	public JPopupMenu getPopupMenu()
	{
		return mnuPopup;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return MouseListener
	 */
	////////////////////////////////////////////////////////
	public MouseListener getPopupListener()
	{
		return lsnPopup;
	}
	////////////////////////////////////////////////////////
	// Utility
	////////////////////////////////////////////////////////
	protected void preparePopupMenu()
	{
		JMenuItem mnuCopy = new JMenuItem();
		JMenuItem mnuInsert = new JMenuItem();
		JMenuItem mnuDelete = new JMenuItem();
		JMenu mnuShow = new JMenu();
		JMenuItem mnuHide = new JMenuItem();
		JMenuItem mnuFilter = new JMenuItem();
		JMenuItem mnuShowAll = new JMenuItem();
		JMenuItem mnuExport = new JMenuItem();
		mnuCopy.setText(DefaultDictionary.getString("VectorTable.Copy"));
		mnuInsert.setText(DefaultDictionary.getString("VectorTable.Insert"));
		mnuDelete.setText(DefaultDictionary.getString("VectorTable.Delete"));
		mnuShow.setText(DefaultDictionary.getString("VectorTable.Show"));
		mnuHide.setText(DefaultDictionary.getString("VectorTable.Hide"));
		mnuExport.setText(DefaultDictionary.getString("VectorTable.Export"));
		mnuFilter.setText(DefaultDictionary.getString("VectorTable.Filter"));
		mnuShowAll.setText(DefaultDictionary.getString("VectorTable.ShowAll"));
		try
		{
			mnuCopy.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/copy.gif")));
			mnuInsert.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/insert.gif")));
			mnuDelete.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/delete.gif")));
			mnuShow.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/show.gif")));
			mnuHide.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/hide.gif")));
			mnuExport.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/export.gif")));
			mnuFilter.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/filter.gif")));
			mnuShowAll.setIcon(new ImageIcon(getClass().getResource("/com/fss/swing/showall.gif")));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		mnuPopup.removeAll();
		mnuPopup.add(mnuCopy);
		mnuPopup.add(mnuInsert);
		mnuPopup.add(mnuDelete);
		mnuPopup.add(mnuShow);
		mnuPopup.add(mnuHide);
		mnuPopup.add(new JSeparator());
		mnuPopup.add(mnuShowAll);
		mnuPopup.add(mnuFilter);
		mnuPopup.add(mnuExport);
		Skin.applySkin(mnuPopup);
		////////////////////////////////////////////////////////
		mvtShowMenu = new Vector();
		for(int iIndex = 0;iIndex < mvtInvisibleColumn.size();iIndex++)
		{
			VectorTableColumn col = (VectorTableColumn)mvtInvisibleColumn.elementAt(iIndex);
			JMenuItem mnu = new JMenuItem(StringUtil.nvl(col.getHeaderValue(),""));
			mnu.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int iIndex = mvtShowMenu.indexOf(e.getSource());
					if(iIndex >= 0)
						showColumn(iIndex);
				}
			});
			Skin.applySkin(mnu);
			mnuShow.add(mnu);
			mvtShowMenu.addElement(mnu);
		}
		////////////////////////////////////////////////////////
		if(!isAllowInsert())
			mnuInsert.setEnabled(false);
		if(!isAllowDelete())
			mnuDelete.setEnabled(false);
		if(!isAllowExport())
			mnuExport.setEnabled(false);
		if(!isAllowFilter())
			mnuFilter.setEnabled(false);
		if(getColumnCount() <= 1 || !isAllowHideColumn())
			mnuHide.setEnabled(false);
		if(mnuShow.getItemCount() <= 0)
		{
			mnuShow.setEnabled(false);
			if(getFilteredData().size() == getData().size())
				mnuShowAll.setEnabled(false);
		}
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		mnuCopy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				copyToClipBoard();
			}
		});
		////////////////////////////////////////////////////////
		mnuInsert.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int iSeleted = getSelectedRow();
				if(iSeleted < 0)
					addRow();
				else
					insertRow(iSeleted);
			}
		});
		////////////////////////////////////////////////////////
		mnuDelete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int iSeleted = getSelectedRow();
				if(iSeleted >= 0 && iSeleted < mvtFilteredData.size())
					deleteRow(iSeleted);
			}
		});
		////////////////////////////////////////////////////////
		mnuHide.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideColumn();
			}
		});
		////////////////////////////////////////////////////////
		mnuFilter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				requestFilterCriteria();
			}
		});
		////////////////////////////////////////////////////////
		mnuShowAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showAll();
			}
		});
		////////////////////////////////////////////////////////
		mnuExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				export();
			}
		});
	}
	////////////////////////////////////////////////////////
	// Sort
	////////////////////////////////////////////////////////
	public void sort(int iColumn,boolean blnSortAsc)
	{
		// Change sorting state
		mbSorting = true;

		int iSelected[] = getSelectedRows();
		Vector vtSelected = new Vector();
		for(int iIndex = 0;iIndex < iSelected.length;iIndex++)
		{
			if(iSelected[iIndex] >= 0 && iSelected[iIndex] < getRowCount())
				vtSelected.addElement(getRow(iSelected[iIndex]));
		}
		Collections.sort(mvtFilteredData,new VectorTableComparator(iColumn,blnSortAsc));

		// Change sorting state
		mbSorting = false;

		// Update UI
		int iSelectedColumn[] = getColumnModel().getSelectedColumns();
		((VectorModel)dataModel).fireTableDataChanged();
		((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
		for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
			getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);

		getSelectionModel().clearSelection();
		boolean bScrolled = false;
		for(int iIndex = 0;iIndex < getRowCount();iIndex++)
		{
			if(vtSelected.indexOf(getRow(iIndex)) >= 0)
			{
				if(!bScrolled)
				{
					changeSelectedRow(iIndex);
					bScrolled = true;
				}
				else
					addRowSelectionInterval(iIndex,iIndex);
			}
		}
		if(getRowCount() > 0 && getSelectedRow() < 0)
			changeSelectedRow(0);
	}
	////////////////////////////////////////////////////////
	// Purpose: Change focus to next editable control
	// Author: Thai Hoang Hiep
	// Date: 19/05/2003
	////////////////////////////////////////////////////////
	public void editNextColumn()
	{
		int iColIndex = 0;
		int iRowIndex = 0;
		if(getColumnCount() > 0 && getRowCount() > 0)
		{
			// Find index of column in table
			TableColumnModel mdl = getColumnModel();
			int iColumnCount = mdl.getColumnCount();
			int iCurrentColIndex = getSelectedColumn();

			// Find next editable column
			boolean bFound = false;
			iColIndex = iCurrentColIndex + 1;
			while(!bFound && iColIndex < iColumnCount)
			{
				VectorTableColumn col = (VectorTableColumn)mdl.getColumn(iColIndex);
				if(col.isEditable())
					bFound = true;
				else
					iColIndex++;
			}
			if(!bFound)
			{
				iColIndex = 0;
				while(!bFound && iColIndex < iCurrentColIndex)
				{
					VectorTableColumn col = (VectorTableColumn)mdl.getColumn(iColIndex);
					if(col.isEditable())
						bFound = true;
					else
						iColIndex++;
				}
			}

			// Move to next editable column
			iRowIndex = getSelectedRow();
			if(iColIndex <= iCurrentColIndex)
			{
				if(iRowIndex > getRowCount() - 1)
					iRowIndex = getRowCount() - 1;
				else
					iRowIndex++;
			}
		}
		else if(getColumnCount() > 0 && getRowCount() == 0)
		{
			iRowIndex = getRowCount() - 1;
			iColIndex = getSelectedColumn();
		}

		if(iRowIndex >= 0 && iRowIndex < getRowCount() &&
		   iColIndex >= 0 && iColIndex < getColumnCount())
		{
			changeSelection(iRowIndex,iColIndex,false,false);
			editCellAt(iRowIndex,iColIndex);
		}
	}
	////////////////////////////////////////////////////////
	public void tableChanged(TableModelEvent e)
	{
		if(!mbSorting)
			super.tableChanged(e);
		if(rowHeader != null)
			rowHeader.refresh(this);
	}
	////////////////////////////////////////////////////////
	public void editCellAtEx(int iRowIndex,int iModelIndex)
	{
		editCellAt(iRowIndex,getColumnIndex(iModelIndex));
	}
	////////////////////////////////////////////////////////
	public boolean editCellAt(int iRowIndex,int iColIndex)
	{
		if(super.editCellAt(iRowIndex,iColIndex))
		{
			if(getSelectedColumn() != iColIndex || getSelectedRow() != iRowIndex)
				changeSelection(iRowIndex,iColIndex,false,false);
			requestFocusInWindow();
			return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////
	// Purpose: return special editor of cell
	// Author: Thai Hoang Hiep
	// Date: 21/10/2003
	////////////////////////////////////////////////////////
	public JComponent getCellEditorComponent(int iRowIndex,int iColIndex)
	{
		return null;
	}
	////////////////////////////////////////////////////////
	// Purpose: return special renderer of cell
	// Author: Thai Hoang Hiep
	// Date: 21/10/2003
	////////////////////////////////////////////////////////
	public JComponent getCellRendererComponent(int iRowIndex,int iColIndex)
	{
		return null;
	}
	////////////////////////////////////////////////////////
	// Purpose: Apply special effect for cell editor
	// Author: Thai Hoang Hiep
	// Date: 21/10/2003
	////////////////////////////////////////////////////////
	public void applyCellEditorComponent(JComponent cmp,int iRowIndex,int iColIndex)
	{
	}
	////////////////////////////////////////////////////////
	// Purpose: Apply special effect for cell renderer
	// Author: Thai Hoang Hiep
	// Date: 21/10/2003
	////////////////////////////////////////////////////////
	public void applyCellRendererComponent(JComponent cmp,int iRowIndex,int iColIndex)
	{
	}
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public Vector vtCurrentData = null;
	private boolean bStored = false;
	////////////////////////////////////////////////////////
	// Purpose: Create new vector store same data with this table data
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public Vector cloneData()
	{
		Vector vtData = getData();
		Vector vtReturn = new Vector();
		for(int iRowIndex = 0;iRowIndex < vtData.size();iRowIndex++)
		{
			Vector vtDataRow = (Vector)vtData.elementAt(iRowIndex);
			Vector vtReturnRow = new Vector();
			for(int iColIndex = 0;iColIndex < vtDataRow.size();iColIndex++)
				vtReturnRow.addElement(vtDataRow.elementAt(iColIndex));
			vtReturn.addElement(vtReturnRow);
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		vtCurrentData = CollectionUtil.cloneVector(getData());
		bStored = true;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public Vector getBackupData()
	{
		return vtCurrentData;
	}
	////////////////////////////////////////////////////////
	// Purpose: Restore value of text field
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void restore()
	{
		if(!bStored)
			return;

		mvtTableData.clear();
		for(int iRowIndex = 0;iRowIndex < vtCurrentData.size();iRowIndex++)
		{
			Vector vtCurrentDataRow = (Vector)vtCurrentData.elementAt(iRowIndex);
			Vector vtDataRow = new Vector();
			for(int iColIndex = 0;iColIndex < vtCurrentDataRow.size();iColIndex++)
				vtDataRow.addElement(vtCurrentDataRow.elementAt(iColIndex));
			mvtTableData.addElement(vtDataRow);
		}
		mvtFilteredData = (Vector)mvtTableData.clone();
		bStored = true;

		clearBackup();
	}
	////////////////////////////////////////////////////////
	// Purpose: Return changed status
	// Return true if text field is changed, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public boolean isChanged()
	{
		if(!bStored)
			return false;
		return !CollectionUtil.isSimilar(getData(),vtCurrentData);
	}
	////////////////////////////////////////////////////////
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		vtCurrentData = null;
		bStored = false;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set data for all row of column
	 * @param iColIndex int
	 * @param strValue String
	 */
	////////////////////////////////////////////////////////
	public void setAllValue(int iColIndex,String strValue)
	{
		for(int iRowIndex = 0;iRowIndex < mvtFilteredData.size();iRowIndex++)
			setValueAt(strValue,iRowIndex,iColIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 * Set data for all row of column
	 * @param iModelIndex int
	 * @param strValue String
	 */
	////////////////////////////////////////////////////////
	public void setAllValueEx(int iModelIndex,String strValue)
	{
		for(int iRowIndex = 0;iRowIndex < mvtFilteredData.size();iRowIndex++)
			getRow(iRowIndex).setElementAt(strValue,iModelIndex);
		repaint();
	}
	////////////////////////////////////////////////////////
	public static void setDefaultRowHeight(int iRowHeight)
	{
		DEFAULT_ROW_HEIGHT = iRowHeight;
	}
	////////////////////////////////////////////////////////
	public static int getDefaultRowHeight()
	{
		return DEFAULT_ROW_HEIGHT;
	}
	////////////////////////////////////////////////////////
	public static void setDefaultHeaderHeight(int iHeaderHeight)
	{
		DEFAULT_HEADER_HEIGHT = iHeaderHeight;
	}
	////////////////////////////////////////////////////////
	public static int getDefaultHeaderHeight()
	{
		return DEFAULT_HEADER_HEIGHT;
	}
	////////////////////////////////////////////////////////
	/**
	 * View and filter support
	 */
	////////////////////////////////////////////////////////
	private Vector mvtFilteredData = new Vector();
	////////////////////////////////////////////////////////
	/**
	 * Get filtered data
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public Vector getFilteredData()
	{
		return mvtFilteredData;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void copyToClipBoard()
	{
		// Build string data
		StringBuffer buf = new StringBuffer();
		int iSelectedColumns[] = null;
		if(getColumnSelectionAllowed())
			iSelectedColumns = getSelectedColumns();
		int iSelectedRows[] = null;
		if(getRowSelectionAllowed())
			iSelectedRows = getSelectedRows();
		int iColumnCount = (iSelectedColumns == null) ? getColumnCount() : iSelectedColumns.length;
		int iRowCount = (iSelectedRows == null) ? mvtFilteredData.size() : iSelectedRows.length;
		for(int iRowIndex = 0;iRowIndex < iRowCount && iRowIndex < 1000;iRowIndex++)
		{
			for(int iColumnIndex = 0;iColumnIndex < iColumnCount - 1 && iColumnIndex < 1000;iColumnIndex++)
			{
				buf.append(StringUtil.nvl(getValueAt((iSelectedRows == null) ? iRowIndex : iSelectedRows[iRowIndex],
													 (iSelectedColumns == null) ? iColumnIndex : iSelectedColumns[iColumnIndex]),""));
				buf.append("\t");
			}
			buf.append(StringUtil.nvl(getValueAt((iSelectedRows == null) ? iRowIndex : iSelectedRows[iRowIndex],
												 (iSelectedColumns == null) ? iColumnCount - 1 : iSelectedColumns[iColumnCount - 1]),""));
			buf.append("\r\n");
		}

		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		java.awt.datatransfer.StringSelection str = new java.awt.datatransfer.StringSelection(buf.toString());
		clipboard.setContents(str,str);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param fl File
	 * @param strSeparator String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void storeCvs(File fl,String strSeparator) throws Exception
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(fl);
			//os.write("ï»¿".getBytes());

			// Create header
			int iColumnCount = getColumnCount();
			if(iColumnCount > 0)
				os.write(StringUtil.nvl(getColumn(0).getHeaderValue(),"").getBytes(Global.ENCODE));
			for(int iColumnIndex = 1;iColumnIndex < iColumnCount;iColumnIndex++)
			{
				os.write(strSeparator.getBytes(Global.ENCODE));
				os.write(StringUtil.nvl(getColumn(iColumnIndex).getHeaderValue(),"").getBytes(Global.ENCODE));
			}
			os.write('\r');
			os.write('\n');

			// Create content
			int iRowCount = mvtFilteredData.size();
			for(int iIndex = 0;iIndex < iRowCount;iIndex++)
			{
				if(iColumnCount > 0)
					os.write(StringUtil.nvl(getValueAt(iIndex,0),"").getBytes(Global.ENCODE));
				for(int iColumnIndex = 1;iColumnIndex < iColumnCount;iColumnIndex++)
				{
					os.write(strSeparator.getBytes(Global.ENCODE));
					os.write(StringUtil.nvl(getValueAt(iIndex,iColumnIndex),"").getBytes(Global.ENCODE));
				}
				os.write('\r');
				os.write('\n');
			}
		}
		finally
		{
			FileUtil.safeClose(os);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Show all data
	 */
	////////////////////////////////////////////////////////
	public void showAll()
	{
		int iSelected[] = getSelectedRows();
		Vector vtSelected = new Vector();
		for(int iIndex = 0;iIndex < iSelected.length;iIndex++)
			vtSelected.addElement(getRow(iSelected[iIndex]));

		// Show all row
		mvtFilteredData = (Vector)mvtTableData.clone();

		// Show all column
		for(int iIndex = 0;iIndex < mvtInvisibleColumn.size();iIndex++)
			addColumn((TableColumn)mvtInvisibleColumn.elementAt(iIndex));
		mvtInvisibleColumn.removeAllElements();

		int iSelectedColumn[] = getColumnModel().getSelectedColumns();
		((VectorModel)dataModel).fireTableDataChanged();
		((VectorModel)dataModel).fireContentsChanged(this,0,getRowCount());
		for(int iIndex = 0;iIndex < iSelectedColumn.length;iIndex++)
			getColumnModel().getSelectionModel().addSelectionInterval(iSelectedColumn[iIndex],iSelectedColumn[iIndex]);

		getSelectionModel().clearSelection();
		boolean bScrolled = false;
		for(int iIndex = 0;iIndex < getRowCount();iIndex++)
		{
			if(vtSelected.indexOf(getRow(iIndex)) >= 0)
			{
				if(!bScrolled)
				{
					changeSelectedRow(iIndex);
					bScrolled = true;
				}
				else
					addRowSelectionInterval(iIndex,iIndex);
			}
		}
		if(getRowCount() > 0 && getSelectedRow() < 0)
			changeSelectedRow(0);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param index0 int
	 * @param index1 int
	 * @author BinhTX
	 */
	////////////////////////////////////////////////////////
	public void setRowSelectionIntervalEx(int index0, int index1)
	{
		selectionModel.setSelectionInterval(index0, index1);
		changeSelection(index0,getSelectedColumn(),true,true);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public Vector getSampleRow()
	{
		return mvtSampleRow;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public boolean isChildVisible()
	{
		return mnuPopup.isVisible() || mbChildVisible;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param e ListSelectionEvent
	 */
	////////////////////////////////////////////////////////
	public void valueChanged(ListSelectionEvent e)
	{
		super.valueChanged(e);
		if(dataModel != null && dataModel instanceof VectorModel)
			((VectorModel)dataModel).fireContentsChanged(this, -1, -1);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iRowIndex int
	 */
	////////////////////////////////////////////////////////
	public void changeSelectedRow(int iRowIndex)
	{
		changeSelection(iRowIndex,getSelectedColumn(),false,false);
	}
	//////////////////////////////////////////////////////////////
	/**
	 *
	 */
	//////////////////////////////////////////////////////////////
	public void updateUI()
	{
		super.updateUI();
		if(mnuPopup != null)
		{
			SwingUtilities.updateComponentTreeUI(mnuPopup);
			Skin.applySkin(mnuPopup);
		}
	}
}
