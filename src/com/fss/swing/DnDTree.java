package com.fss.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DnDTree extends JTree implements DragGestureListener,DropTargetListener,DragSourceListener
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private int miLastDragOverRow = -1;
	private boolean mbLastDragOverSelected = false;
	protected boolean mbAllowCopy = false;
	protected boolean mbAllowMove = false;
	protected boolean mbAllowLocalDrop = true;
	protected boolean mbAllowExternalDrop = true;
	protected DragGestureRecognizer recognizer;
	////////////////////////////////////////////////////////
	protected TreePath[] pathSource = null;
	protected TreePath pathDestination = null;
	protected DefaultMutableTreeNode mndDestination = null;
	protected int miLocation;
	////////////////////////////////////////////////////////
	public DnDTree()
	{
		// Initialize
		super((TreeNode)null);

		// Create drag source recognizer
		recognizer = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_NONE,this);

		// Create drop target
		new DropTarget(this,this);
	}
	////////////////////////////////////////////////////////
	public void setRoot(DefaultMutableTreeNode nd)
	{
		((DefaultTreeModel)getModel()).setRoot(nd);
	}
	////////////////////////////////////////////////////////
	public DefaultMutableTreeNode getRoot()
	{
		return (DefaultMutableTreeNode)((DefaultTreeModel)getModel()).getRoot();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void createDragSource()
	{
		if(isEnabled())
		{
			if(getAllowMove() && getAllowCopy())
				recognizer.setSourceActions(DnDConstants.ACTION_COPY_OR_MOVE);
			else if(getAllowMove())
				recognizer.setSourceActions(DnDConstants.ACTION_MOVE);
			else if(getAllowCopy())
				recognizer.setSourceActions(DnDConstants.ACTION_COPY);
			else
				recognizer.setSourceActions(DnDConstants.ACTION_NONE);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bAllowMove boolean
	 */
	////////////////////////////////////////////////////////
	public void setAllowMove(boolean bAllowMove)
	{
		mbAllowMove = bAllowMove;
		createDragSource();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean getAllowMove()
	{
		return mbAllowMove;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bAllowMove boolean
	 */
	////////////////////////////////////////////////////////
	public void setAllowCopy(boolean bAllowCopy)
	{
		mbAllowCopy = bAllowCopy;
		createDragSource();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean getAllowCopy()
	{
		return mbAllowCopy;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bAllowMove boolean
	 */
	////////////////////////////////////////////////////////
	public void setAllowLocalDrop(boolean bAllowLocalDrop)
	{
		mbAllowLocalDrop = bAllowLocalDrop;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean getAllowLocalDrop()
	{
		return mbAllowLocalDrop;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bAllowMove boolean
	 */
	////////////////////////////////////////////////////////
	public void setAllowExternalDrop(boolean bAllowExternalDrop)
	{
		mbAllowCopy = bAllowExternalDrop;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean getAllowExternalDrop()
	{
		return mbAllowExternalDrop;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param paths TreePath[]
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	protected boolean isDropAllowed(TreePath[] paths)
	{
		return true;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param paths TreePath[]
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	protected Vector drop(TreePath[] paths,int iLocation)
	{
		// Drop item
		Vector vtDroped = new Vector();
		clearSelection();
		for(int iIndex = 0;iIndex < paths.length;iIndex++)
		{
			if(pathSource != null)
			{
				if(pathSource[iIndex].getParentPath() == null || pathSource[iIndex].isDescendant(pathDestination))
					continue;
			}
			DefaultMutableTreeNode ndSource = (DefaultMutableTreeNode)paths[iIndex].getLastPathComponent();
			((DefaultTreeModel)getModel()).insertNodeInto(ndSource,mndDestination,iLocation + iIndex);
			if(pathDestination != null)
				addSelectionPath(pathDestination.pathByAddingChild(ndSource));
			vtDroped.addElement(pathSource[iIndex]);
		}
		return vtDroped;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtDroped Vector
	 * @param e DropTargetDragEvent
	 */
	////////////////////////////////////////////////////////
	protected void completeDrop(Vector vtDroped,DropTargetDropEvent e)
	{
		// Correct pathSource
		if(pathSource != null)
		{
			pathSource = new TreePath[vtDroped.size()];
			for(int iIndex = 0;iIndex < pathSource.length;iIndex++)
				pathSource[iIndex] = (TreePath)vtDroped.elementAt(iIndex);
		}
		e.getDropTargetContext().dropComplete(true);
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void dragGestureRecognized(DragGestureEvent e)
	{
		if(isEnabled())
		{
			pathSource = this.getSelectionPaths();
			if(pathSource == null || pathSource.length <= 0)
				return;
			int iModifier = e.getTriggerEvent().getModifiersEx();
			if((iModifier & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
				return;
			TransferableData data = new TransferableData(pathSource);
			int iAction = e.getDragAction();
			if(iAction == DnDConstants.ACTION_MOVE && getAllowMove())
				e.getDragSource().startDrag(e,DragSource.DefaultMoveDrop,data,this);
			else if(iAction == DnDConstants.ACTION_COPY && getAllowCopy())
				e.getDragSource().startDrag(e,DragSource.DefaultCopyDrop,data,this);
		}
	}
	////////////////////////////////////////////////////////
	public void dragDropEnd(DragSourceDropEvent dsde)
	{
		if(dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE && pathSource != null)
		{
			for(int iIndex = 0;iIndex < pathSource.length;iIndex++)
			{
				DefaultMutableTreeNode ndSource = ((DefaultMutableTreeNode)pathSource[iIndex].getLastPathComponent());
				((DefaultTreeModel)getModel()).removeNodeFromParent(ndSource);
			}
		}
		pathSource = null;
	}
	////////////////////////////////////////////////////////
	public void dragEnter(DragSourceDragEvent dsde)
	{
	}
	////////////////////////////////////////////////////////
	public void dragOver(DragSourceDragEvent dsde)
	{
	}
	////////////////////////////////////////////////////////
	public void dropActionChanged(DragSourceDragEvent dsde)
	{
		if(dsde.getUserAction() == DnDConstants.ACTION_MOVE)
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
		else if(dsde.getUserAction() == DnDConstants.ACTION_COPY)
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
	}
	////////////////////////////////////////////////////////
	public void dragExit(DragSourceEvent dsde)
	{
	}
	////////////////////////////////////////////////////////
	/** DropTargetListener interface method - What we do when drag is released */
	////////////////////////////////////////////////////////
	public void drop(DropTargetDropEvent e)
	{
		// Check enabled
		if(!isEnabled())
		{
			e.rejectDrop();
			return;
		}

		// Check right
		if((pathSource == null && !getAllowExternalDrop()) ||
		   (pathSource != null && !getAllowLocalDrop()))
		{
			e.rejectDrop();
			return;
		}

		// Reset selection state
		if(miLastDragOverRow >= 0)
		{
			if(!mbLastDragOverSelected)
				removeSelectionRow(miLastDragOverRow);
			miLastDragOverRow = -1;
		}

		// Do drop
		try
		{
			// Flavor not supported, reject drop
			Transferable tr = e.getTransferable();
			if(!tr.isDataFlavorSupported(TransferableData.TRANSFERABLE_DATA_FLAVOR))
			{
				e.rejectDrop();
				return;
			}

			// Cast into appropriate data type
			Object obj = tr.getTransferData(TransferableData.TRANSFERABLE_DATA_FLAVOR);
			if(obj == null || !(obj instanceof TreePath[]))
			{
				e.rejectDrop();
				return;
			}
			TreePath[] paths = (TreePath[])obj;

			// Get new parent node
			Point pt = e.getLocation();
			pathDestination = getPathForLocation(pt.x,pt.y);
			if(pathDestination == null)
				mndDestination = getRoot();
			else
				mndDestination = (DefaultMutableTreeNode)pathDestination.getLastPathComponent();
			miLocation = mndDestination.getChildCount();
			if(!mndDestination.getAllowsChildren())
			{
				miLocation = mndDestination.getParent().getIndex(mndDestination);
				mndDestination = (DefaultMutableTreeNode)mndDestination.getParent();
				pathDestination = pathDestination.getParentPath();
			}

			// Check destination
			if(!isDropAllowed(paths))
			{
				e.rejectDrop();
				return;
			}

			// Drop item
			e.acceptDrop(e.getDropAction());
			Vector vtDroped = drop(paths,miLocation);

			// Complete drop
			completeDrop(vtDroped,e);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			e.rejectDrop();
		}
	}
	////////////////////////////////////////////////////////
	public void dragEnter(DropTargetDragEvent e)
	{
	}
	////////////////////////////////////////////////////////
	public void dragExit(DropTargetEvent e)
	{
		if(miLastDragOverRow >= 0)
		{
			if(!mbLastDragOverSelected)
				removeSelectionRow(miLastDragOverRow);
			miLastDragOverRow = -1;
		}
	}
	////////////////////////////////////////////////////////
	public void dragOver(DropTargetDragEvent e)
	{
		Point pt = e.getLocation();
		pathDestination = getPathForLocation(pt.x,pt.y);
		if(pathDestination == null)
		{
			if(miLastDragOverRow >= 0)
			{
				if(!mbLastDragOverSelected)
					removeSelectionRow(miLastDragOverRow);
				miLastDragOverRow = -1;
			}
			return;
		}
		int iRowIndex = getRowForPath(pathDestination);
		if(miLastDragOverRow == iRowIndex)
			return;
		if(miLastDragOverRow >= 0)
		{
			if(!mbLastDragOverSelected)
				removeSelectionRow(miLastDragOverRow);
		}
		miLastDragOverRow = iRowIndex;
		mbLastDragOverSelected = isRowSelected(miLastDragOverRow);
		if(mbLastDragOverSelected)
			removeSelectionRow(miLastDragOverRow);
		addSelectionRow(miLastDragOverRow);
	}
	////////////////////////////////////////////////////////
	public void dropActionChanged(DropTargetDragEvent e)
	{
	}
}
