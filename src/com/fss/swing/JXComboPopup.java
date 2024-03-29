package com.fss.swing;

/*
 * @(#)JXComboPopup.java	1.73 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This is a basic implementation of the <code>ComboPopup</code> interface.
 *
 * This class represents the ui for the popup portion of the combo box.
 * <p>
 * All event handling is handled by listener classes created with the
 * <code>createxxxListener()</code> methods and internal classes.
 * You can change the behavior of this class by overriding the
 * <code>createxxxListener()</code> methods and supplying your own
 * event listeners or subclassing from the ones supplied in this class.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version 1.73 01/23/03
 * @author Tom Santos
 * @author Mark Davidson
 * @author Thai Hoang Hiep - Change to use with VectorTable
 */

public class JXComboPopup extends JPopupMenu
{
	protected JXCombo comboBox;
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the accessor methods instead.
	 *
	 * @see #getList
	 * @see #createList
	 */
	protected VectorTable list;
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead
	 *
	 * @see #createScroller
	 */
	protected JScrollPane scroller;

	/**
	 * As of Java 2 platform v1.4 this previously undocumented field is no
	 * longer used.
	 */
	protected boolean valueIsAdjusting = false;

	// Listeners that are required by the ComboPopup interface
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the accessor or create methods instead.
	 *
	 * @see #getMouseMotionListener
	 * @see #createMouseMotionListener
	 */
	//protected MouseMotionListener mouseMotionListener;
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the accessor or create methods instead.
	 *
	 * @see #getMouseListener
	 * @see #createMouseListener
	 */
	protected MouseListener mouseListener;

	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the accessor or create methods instead.
	 *
	 * @see #getKeyListener
	 * @see #createKeyListener
	 */
	protected KeyListener keyListener;

	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead.
	 *
	 * @see #createListSelectionListener
	 */
	protected ListSelectionListener listSelectionListener;

	// Listeners that are attached to the list
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead.
	 *
	 * @see #createListMouseListener
	 */
	protected MouseListener listMouseListener;

	// Added to the combo box for bound properties
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead
	 *
	 * @see #createPropertyChangeListener
	 */
	protected PropertyChangeListener propertyChangeListener;

	// Added to the combo box model
	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead
	 *
	 * @see #createListDataListener
	 */
	protected ListDataListener listDataListener;

	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override. Use the create method instead
	 *
	 * @see #createItemListener
	 */
	protected ItemListener itemListener;

	/**
	 * This protected field is implementation specific. Do not access directly
	 * or override.
	 */
	protected Timer autoscrollTimer;
	protected boolean hasEntered = false;
	protected boolean isAutoScrolling = false;
	protected int scrollDirection = SCROLL_UP;

	protected static final int SCROLL_UP = 0;
	protected static final int SCROLL_DOWN = 1;

	//========================================
	// begin ComboPopup method implementations
	//

	/**
	 * Implementation of ComboPopup.show().
	 */
	public void show()
	{
		setListSelection(comboBox.getSelectedIndex());
		Point location = getPopupLocation();
		show(comboBox,location.x,location.y);
	}

	/**
	 * Implementation of ComboPopup.hide().
	 */
	public void hide()
	{
		MenuSelectionManager manager = MenuSelectionManager.defaultManager();
		MenuElement[] selection = manager.getSelectedPath();
		for(int i = 0;i < selection.length;i++)
		{
			if(selection[i] == this)
			{
				manager.clearSelectedPath();
				break;
			}
		}
		super.setVisible(false);
		if(selection.length > 0)
			comboBox.repaint();
	}

	/**
	 * Implementation of ComboPopup.getList().
	 */
	public VectorTable getList()
	{
		return list;
	}

	/**
	 * Implementation of ComboPopup.getMouseListener().
	 *
	 * @return a <code>MouseListener</code> or null
	 * @see ComboPopup#getMouseListener
	 */
	public MouseListener getMouseListener()
	{
		if(mouseListener == null)
			mouseListener = createMouseListener();
		return mouseListener;
	}

	/**
	 * Implementation of ComboPopup.getMouseMotionListener().
	 *
	 * @return a <code>MouseMotionListener</code> or null
	 * @see ComboPopup#getMouseMotionListener
	 */
	/*public MouseMotionListener getMouseMotionListener()
	{
		if(mouseMotionListener == null)
			mouseMotionListener = createMouseMotionListener();
		return mouseMotionListener;
	}*/

	/**
	 * Implementation of ComboPopup.getKeyListener().
	 *
	 * @return a <code>KeyListener</code> or null
	 * @see ComboPopup#getKeyListener
	 */
	public KeyListener getKeyListener()
	{
		if(keyListener == null)
			keyListener = createKeyListener();
		return keyListener;
	}

	/**
	 * Called when the UI is uninstalling.  Since this popup isn't in the component
	 * tree, it won't get it's uninstallUI() called.  It removes the listeners that
	 * were added in addComboBoxListeners().
	 */
	public void uninstallingUI()
	{
		if(propertyChangeListener != null)
		{
			comboBox.removePropertyChangeListener(propertyChangeListener);
		}
		if(itemListener != null)
		{
			comboBox.removeItemListener(itemListener);
		}
		uninstallComboBoxModelListeners(comboBox.getModel());
		uninstallKeyboardActions();
		uninstallListListeners();
		// We do this, otherwise the listener the ui installs on
		// the model (the combobox model in this case) will keep a
		// reference to the list, causing the list (and us) to never get gced.
		// HiepTH list.setModel(EmptyListModel);
	}

	//
	// end ComboPopup method implementations
	//======================================

	/**
	 * Removes the listeners from the combo box model
	 *
	 * @param model The combo box model to install listeners
	 * @see #installComboBoxModelListeners
	 */
	protected void uninstallComboBoxModelListeners(ComboBoxModel model)
	{
		if(model != null && listDataListener != null)
			model.removeListDataListener(listDataListener);
	}

	protected void uninstallKeyboardActions()
	{
	}

	//===================================================================
	// begin Initialization routines
	//
	public JXComboPopup(JXCombo combo)
	{
		super();
		comboBox = combo;

		installComboBoxListeners();
		setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

		// UI construction of the popup.
		list = combo.getPopupTable();
		configureList();
		scroller = createScroller();
		configureScroller();
		configurePopup();

		installKeyboardActions();
	}

	// Overriden PopupMenuListener notification methods to inform combo box
	// PopupMenuListeners.

	protected void firePopupMenuWillBecomeVisible()
	{
		super.firePopupMenuWillBecomeVisible();
		comboBox.firePopupMenuWillBecomeVisible();
	}

	protected void firePopupMenuWillBecomeInvisible()
	{
		super.firePopupMenuWillBecomeInvisible();
		comboBox.firePopupMenuWillBecomeInvisible();
	}

	protected void firePopupMenuCanceled()
	{
		super.firePopupMenuCanceled();
		comboBox.firePopupMenuCanceled();
	}

	/**
	 * Creates a listener
	 * that will watch for mouse-press and release events on the combo box.
	 *
	 * <strong>Warning:</strong>
	 * When overriding this method, make sure to maintain the existing
	 * behavior.
	 *
	 * @return a <code>MouseListener</code> which will be added to
	 * the combo box or null
	 */
	protected MouseListener createMouseListener()
	{
		return new InvocationMouseHandler();
	}

	/**
	 * Creates the mouse motion listener which will be added to the combo
	 * box.
	 *
	 * <strong>Warning:</strong>
	 * When overriding this method, make sure to maintain the existing
	 * behavior.
	 *
	 * @return a <code>MouseMotionListener</code> which will be added to
	 *         the combo box or null
	 */
	/*protected MouseMotionListener createMouseMotionListener()
	{
		return new InvocationMouseMotionHandler();
	}*/

	/**
	 * Creates the key listener that will be added to the combo box. If
	 * this method returns null then it will not be added to the combo box.
	 *
	 * @return a <code>KeyListener</code> or null
	 */
	protected KeyListener createKeyListener()
	{
		return null;
	}

	/**
	 * Creates a list selection listener that watches for selection changes in
	 * the popup's list.  If this method returns null then it will not
	 * be added to the popup list.
	 *
	 * @return an instance of a <code>ListSelectionListener</code> or null
	 */
	protected ListSelectionListener createListSelectionListener()
	{
		return null;
	}

	/**
	 * Creates a list data listener which will be added to the
	 * <code>ComboBoxModel</code>. If this method returns null then
	 * it will not be added to the combo box model.
	 *
	 * @return an instance of a <code>ListDataListener</code> or null
	 */
	protected ListDataListener createListDataListener()
	{
		return null;
	}

	/**
	 * Creates a mouse listener that watches for mouse events in
	 * the popup's list. If this method returns null then it will
	 * not be added to the combo box.
	 *
	 * @return an instance of a <code>MouseListener</code> or null
	 */
	protected MouseListener createListMouseListener()
	{
		return new Handler();
	}

	/**
	 * Creates a <code>PropertyChangeListener</code> which will be added to
	 * the combo box. If this method returns null then it will not
	 * be added to the combo box.
	 *
	 * @return an instance of a <code>PropertyChangeListener</code> or null
	 */
	protected PropertyChangeListener createPropertyChangeListener()
	{
		return new Handler();
	}

	/**
	 * Creates an <code>ItemListener</code> which will be added to the
	 * combo box. If this method returns null then it will not
	 * be added to the combo box.
	 * <p>
	 * Subclasses may override this method to return instances of their own
	 * ItemEvent handlers.
	 *
	 * @return an instance of an <code>ItemListener</code> or null
	 */
	protected ItemListener createItemListener()
	{
		return new Handler();
	}

	/**
	 * Configures the list which is used to hold the combo box items in the
	 * popup. This method is called when the UI class
	 * is created.
	 *
	 * @see #createList
	 */
	protected void configureList()
	{
		list.setFont(comboBox.getFont());
		list.setForeground(comboBox.getForeground());
		list.setBackground(comboBox.getBackground());
		list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
		list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));
		list.setBorder(null);
		// HiepTH list.setCellRenderer(comboBox.getRenderer());
		list.setFocusable(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setListSelection(comboBox.getSelectedIndex());
		installListListeners();
	}

	/**
	 * Adds the listeners to the list control.
	 */
	protected void installListListeners()
	{
		if((listMouseListener = createListMouseListener()) != null)
			list.addMouseListener(listMouseListener);
		if((listSelectionListener = createListSelectionListener()) != null)
			list.getSelectionModel().addListSelectionListener(listSelectionListener);
	}

	void uninstallListListeners()
	{
		if(listMouseListener != null)
		{
			list.removeMouseListener(listMouseListener);
			listMouseListener = null;
		}
		if(listSelectionListener != null)
		{
			list.getSelectionModel().removeListSelectionListener(listSelectionListener);
			listSelectionListener = null;
		}
	}

	/**
	 * Creates the scroll pane which houses the scrollable list.
	 */
	protected JScrollPane createScroller()
	{
		return new JScrollPane(list,
							   ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
							   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	/**
	 * Configures the scrollable portion which holds the list within
	 * the combo box popup. This method is called when the UI class
	 * is created.
	 */
	protected void configureScroller()
	{
		scroller.setFocusable(false);
		scroller.getVerticalScrollBar().setFocusable(false);
		scroller.setBorder(BorderFactory.createEmptyBorder());
	}

	/**
	 * Configures the popup portion of the combo box. This method is called
	 * when the UI class is created.
	 */
	protected void configurePopup()
	{
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorderPainted(true);
		setBorder(BorderFactory.createLineBorder(Color.black));
		setOpaque(false);
		add(scroller);
		setDoubleBuffered(true);
		setFocusable(false);
	}

	/**
	 * This method adds the necessary listeners to the JXCombo.
	 */
	protected void installComboBoxListeners()
	{
		if((propertyChangeListener = createPropertyChangeListener()) != null)
			comboBox.addPropertyChangeListener(propertyChangeListener);
		if((itemListener = createItemListener()) != null)
			comboBox.addItemListener(itemListener);
		installComboBoxModelListeners(comboBox.getModel());
	}

	/**
	 * Installs the listeners on the combo box model. Any listeners installed
	 * on the combo box model should be removed in
	 * <code>uninstallComboBoxModelListeners</code>.
	 *
	 * @param model The combo box model to install listeners
	 * @see #uninstallComboBoxModelListeners
	 */
	protected void installComboBoxModelListeners(ComboBoxModel model)
	{
		if(model != null && (listDataListener = createListDataListener()) != null)
			model.addListDataListener(listDataListener);
	}

	protected void installKeyboardActions()
	{

		/* XXX - shouldn't call this method. take it out for testing.
		   ActionListener action = new ActionListener() {
		 public void actionPerformed(ActionEvent e){
		 }
		   };

		   comboBox.registerKeyboardAction( action,
				 KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ),
				 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ); */

	}

	//
	// end Initialization routines
	//=================================================================


	//===================================================================
	// begin Event Listenters
	//

	/**
	 * A listener to be registered upon the combo box
	 * (<em>not</em> its popup menu)
	 * to handle mouse events
	 * that affect the state of the popup menu.
	 * The main purpose of this listener is to make the popup menu
	 * appear and disappear.
	 * This listener also helps
	 * with click-and-drag scenarios by setting the selection if the mouse was
	 * released over the list during a drag.
	 *
	 * <p>
	 * <strong>Warning:</strong>
	 * We recommend that you <em>not</em>
	 * create subclasses of this class.
	 * If you absolutely must create a subclass,
	 * be sure to invoke the superclass
	 * version of each method.
	 *
	 * @see JXComboPopup#createMouseListener
	 */
	protected class InvocationMouseHandler extends MouseAdapter
	{
		/**
		 * Responds to mouse-pressed events on the combo box.
		 *
		 * @param e the mouse-press event to be handled
		 */
		public void mousePressed(MouseEvent e)
		{
			if(!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled())
				return;

			if(comboBox.isEditable())
			{
				Component comp = comboBox.getEditor().getEditorComponent();
				if((!(comp instanceof JComponent)) || ((JComponent)comp).isRequestFocusEnabled())
					comp.requestFocus();
			}
			else if(comboBox.isRequestFocusEnabled())
				comboBox.requestFocus();
			togglePopup();
		}

		/**
		 * Responds to the user terminating
		 * a click or drag that began on the combo box.
		 *
		 * @param e the mouse-release event to be handled
		 */
		public void mouseReleased(MouseEvent e)
		{
			Component source = (Component)e.getSource();
			Dimension size = source.getSize();
			Rectangle bounds = new Rectangle(0,0,size.width - 1,size.height - 1);
			if(!bounds.contains(e.getPoint()))
			{
				MouseEvent newEvent = convertMouseEvent(e);
				Point location = newEvent.getPoint();
				Rectangle r = new Rectangle();
				list.computeVisibleRect(r);
				if(r.contains(location))
					comboBox.setSelectedIndex(list.rowAtPoint(location));
				comboBox.setPopupVisible(false);
			}
			hasEntered = false;
			stopAutoScrolling();
		}
	}

	/**
	 * This listener watches for dragging and updates the current selection in the
	 * list if it is dragging over the list.
	 */
	/*protected class InvocationMouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseDragged(MouseEvent e)
		{
			if(isVisible())
			{
				MouseEvent newEvent = convertMouseEvent(e);
				Rectangle r = new Rectangle();
				list.computeVisibleRect(r);

				if(newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1)
				{
					hasEntered = true;
					if(isAutoScrolling)
						stopAutoScrolling();
					Point location = newEvent.getPoint();
					if(r.contains(location))
						updateListBoxSelectionForEvent(newEvent,false);
				}
				else
				{
					if(hasEntered)
					{
						int directionToScroll = newEvent.getPoint().y < r.y ? SCROLL_UP : SCROLL_DOWN;
						if(isAutoScrolling && scrollDirection != directionToScroll)
						{
							stopAutoScrolling();
							startAutoScrolling(directionToScroll);
						}
						else if(!isAutoScrolling)
							startAutoScrolling(directionToScroll);
					}
					else
					{
						if(e.getPoint().y < 0)
						{
							hasEntered = true;
							startAutoScrolling(SCROLL_UP);
						}
					}
				}
			}
		}
	}*/

	/**
	 * As of Java 2 platform v 1.4, this class is now obsolete and is only included for
	 * backwards API compatibility. Do not instantiate or subclass.
	 * <p>
	 * All the functionality of this class has been included in
	 * BasicComboBoxUI ActionMap/InputMap methods.
	 */
	/*public class InvocationKeyHandler extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{}
	}*/

	/**
	 * As of Java 2 platform v 1.4, this class is now obsolete, doesn't do anything, and
	 * is only included for backwards API compatibility. Do not call or
	 * override.
	 */
	/*protected class ListSelectionHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{}
	}*/

	/**
	 * As of 1.4, this class is now obsolete, doesn't do anything, and
	 * is only included for backwards API compatibility. Do not call or
	 * override.
	 * <p>
	 * The functionality has been migrated into <code>ItemHandler</code>.
	 *
	 * @see #createItemListener
	 */
	/*public class ListDataHandler implements ListDataListener
	{
		public void contentsChanged(ListDataEvent e)
		{}

		public void intervalAdded(ListDataEvent e)
		{
		}

		public void intervalRemoved(ListDataEvent e)
		{
		}
	}*/

	/**
	 * This listener hides the popup when the mouse is released in the list.
	 */
	protected class Handler extends MouseAdapter implements ItemListener,PropertyChangeListener
	{
		public void mouseReleased(MouseEvent anEvent)
		{
			comboBox.setSelectedIndex(list.getSelectedRow());
			if(anEvent.getButton() != anEvent.BUTTON3)
				comboBox.setPopupVisible(false);
			// workaround for cancelling an edited item (bug 4530953)
			if(comboBox.isEditable() && comboBox.getEditor() != null)
			{
				comboBox.configureEditor(comboBox.getEditor(),
										 comboBox.getSelectedItem());
			}
		}

		public void itemStateChanged(ItemEvent e)
		{
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				JXCombo comboBox = (JXCombo)e.getSource();
				setListSelection(comboBox.getSelectedIndex());
			}
		}

		public void propertyChange(PropertyChangeEvent e)
		{
			JXCombo comboBox = (JXCombo)e.getSource();
			String propertyName = e.getPropertyName();

			if(propertyName.equals("model"))
			{
				ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
				ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
				uninstallComboBoxModelListeners(oldModel);
				installComboBoxModelListeners(newModel);

				list.setModel((VectorModel)newModel);
				comboBox.setModel((VectorModel)newModel);

				if(isVisible())
				{
					hide();
				}
			}
			else if(propertyName.equals("renderer"))
			{
				// HiepTH list.setCellRenderer(comboBox.getRenderer());
				if(isVisible())
					hide();
			}
			else if(propertyName.equals("componentOrientation"))
			{
				// Pass along the new component orientation
				// to the list and the scroller

				ComponentOrientation o = (ComponentOrientation)e.getNewValue();

				VectorTable list = getList();
				if(list != null && list.getComponentOrientation() != o)
				{
					list.setComponentOrientation(o);
				}

				if(scroller != null && scroller.getComponentOrientation() != o)
				{
					scroller.setComponentOrientation(o);
				}

				if(o != getComponentOrientation())
				{
					setComponentOrientation(o);
				}
			}
			else if(propertyName.equals("lightWeightPopupEnabled"))
			{
				setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
			}
		}
	}

	/**
	 * This listener changes the selected item as you move the mouse over the list.
	 * The selection change is not committed to the model, this is for user feedback only.
	 */
	/*protected class ListMouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent anEvent)
		{
			Point location = anEvent.getPoint();
			Rectangle r = new Rectangle();
			list.computeVisibleRect(r);
			if(r.contains(location))
				updateListBoxSelectionForEvent(anEvent,false);
		}
	}*/

	/**
	 * This listener watches for changes to the selection in the
	 * combo box.
	 */
	/*protected class ItemHandler implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				JXCombo comboBox = (JXCombo)e.getSource();
				setListSelection(comboBox.getSelectedIndex());
			}
		}
	}*/

	/**
	 * This listener watches for bound properties that have changed in the
	 * combo box.
	 * <p>
	 * Subclasses which wish to listen to combo box property changes should
	 * call the superclass methods to ensure that the combo popup correctly
	 * handles property changes.
	 *
	 * @see #createPropertyChangeListener
	 */
	/*protected class PropertyChangeHandler implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			JXCombo comboBox = (JXCombo)e.getSource();
			String propertyName = e.getPropertyName();

			if(propertyName.equals("model"))
			{
				ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
				ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
				uninstallComboBoxModelListeners(oldModel);
				installComboBoxModelListeners(newModel);

				list.setModel((VectorModel)newModel);
				comboBox.setModel((VectorModel)newModel);

				if(isVisible())
				{
					hide();
				}
			}
			else if(propertyName.equals("renderer"))
			{
				// HiepTH list.setCellRenderer(comboBox.getRenderer());
				if(isVisible())
					hide();
			}
			else if(propertyName.equals("componentOrientation"))
			{
				// Pass along the new component orientation
				// to the list and the scroller

				ComponentOrientation o = (ComponentOrientation)e.getNewValue();

				VectorTable list = getList();
				if(list != null && list.getComponentOrientation() != o)
				{
					list.setComponentOrientation(o);
				}

				if(scroller != null && scroller.getComponentOrientation() != o)
				{
					scroller.setComponentOrientation(o);
				}

				if(o != getComponentOrientation())
				{
					setComponentOrientation(o);
				}
			}
			else if(propertyName.equals("lightWeightPopupEnabled"))
			{
				setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
			}
		}
	}*/

	//
	// end Event Listeners
	//=================================================================


	/**
	 * Overridden to unconditionally return false.
	 */
	public boolean isFocusTraversable()
	{
		return false;
	}

	//===================================================================
	// begin Autoscroll methods
	//

	/**
	 * This protected method is implementation specific and should be private.
	 * do not call or override.
	 */
	protected void startAutoScrolling(int direction)
	{
		// XXX - should be a private method within InvocationMouseMotionHandler
		// if possible.
		if(isAutoScrolling)
			autoscrollTimer.stop();
		isAutoScrolling = true;

		if(direction == SCROLL_UP)
		{
			scrollDirection = SCROLL_UP;
			Point convertedPoint = SwingUtilities.convertPoint(scroller,new Point(1,1),list);
			int top = list.rowAtPoint(convertedPoint);
			list.changeSelectedRow(top);

			ActionListener timerAction = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					autoScrollUp();
				}
			};

			autoscrollTimer = new Timer(100,timerAction);
		}
		else if(direction == SCROLL_DOWN)
		{
			scrollDirection = SCROLL_DOWN;
			Dimension size = scroller.getSize();
			Point convertedPoint = SwingUtilities.convertPoint(scroller,
				new Point(1,(size.height - 1) - 2),
				list);
			int bottom = list.rowAtPoint(convertedPoint);
			list.changeSelectedRow(bottom);

			ActionListener timerAction = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					autoScrollDown();
				}
			};

			autoscrollTimer = new Timer(100,timerAction);
		}
		autoscrollTimer.start();
	}

	/**
	 * This protected method is implementation specific and should be private.
	 * do not call or override.
	 */
	protected void stopAutoScrolling()
	{
		isAutoScrolling = false;

		if(autoscrollTimer != null)
		{
			autoscrollTimer.stop();
			autoscrollTimer = null;
		}
	}

	/**
	 * This protected method is implementation specific and should be private.
	 * do not call or override.
	 */
	protected void autoScrollUp()
	{
		int index = list.getSelectedRow();
		if(index > 0)
			list.changeSelectedRow(index - 1);
	}

	/**
	 * This protected method is implementation specific and should be private.
	 * do not call or override.
	 */
	protected void autoScrollDown()
	{
		int index = list.getSelectedRow();
		int lastItem = list.getModel().getRowCount() - 1;
		if(index < lastItem)
			list.changeSelectedRow(index + 1);
	}

	//
	// end Autoscroll methods
	//=================================================================


	//===================================================================
	// begin Utility methods
	//

	/**
	 * This is is a utility method that helps event handlers figure out where to
	 * send the focus when the popup is brought up.  The standard implementation
	 * delegates the focus to the editor (if the combo box is editable) or to
	 * the JXCombo if it is not editable.
	 */
	protected void delegateFocus(MouseEvent e)
	{
		if(comboBox.isEditable())
		{
			Component comp = comboBox.getEditor().getEditorComponent();
			if((!(comp instanceof JComponent)) || ((JComponent)comp).isRequestFocusEnabled())
			{
				comp.requestFocus();
			}
		}
		else if(comboBox.isRequestFocusEnabled())
		{
			comboBox.requestFocus();
		}
	}

	/**
	 * Makes the popup visible if it is hidden and makes it hidden if it is
	 * visible.
	 */
	protected void togglePopup()
	{
		if(isVisible())
			hide();
		else
			show();
	}

	/**
	 * Sets the list selection index to the selectedIndex. This
	 * method is used to synchronize the list selection with the
	 * combo box selection.
	 *
	 * @param selectedIndex the index to set the list
	 */
	private void setListSelection(int selectedIndex)
	{
		if(selectedIndex == -1)
			list.getSelectionModel().clearSelection();
		else
			list.changeSelectedRow(selectedIndex);
	}

	protected MouseEvent convertMouseEvent(MouseEvent e)
	{
		Point convertedPoint = SwingUtilities.convertPoint((Component)e.getSource(),
			e.getPoint(),list);
		MouseEvent newEvent = new MouseEvent((Component)e.getSource(),
											 e.getID(),
											 e.getWhen(),
											 e.getModifiers(),
											 convertedPoint.x,
											 convertedPoint.y,
											 e.getClickCount(),
											 e.isPopupTrigger());
		return newEvent;
	}

	/**
	 * Retrieves the height of the popup based on the current
	 * ListCellRenderer and the maximum row count.
	 */
	protected int getPopupHeightForRowCount(int maxRowCount)
	{
		// Set the cached value of the minimum row count
		int minRowCount = Math.min(maxRowCount,comboBox.getItemCount());
		int height = 0;
		JViewport hdr = scroller.getColumnHeader();
		if(hdr != null)
			height = hdr.getHeight();
		else
		{
			boolean bFound = false;
			for(int iIndex = 0;iIndex < list.getColumnCount() && !bFound;iIndex++)
			{
				Object obj = list.getColumn(iIndex).getHeaderValue();
				if(obj != null && !obj.equals(""))
					bFound = true;
			}
			if(bFound)
				height = 20;
			else
				height = 6;
		}
		for(int i = 0;i < minRowCount;++i)
			height += list.getRowHeight(i);
		return height == 0 ? 100 : height;
	}

	/**
	 * Calculate the placement and size of the popup portion of the combo box based
	 * on the combo box location and the enclosing screen bounds. If
	 * no transformations are required, then the returned rectangle will
	 * have the same values as the parameters.
	 *
	 * @param px starting x location
	 * @param py starting y location
	 * @param pw starting width
	 * @param ph starting height
	 * @return a rectangle which represents the placement and size of the popup
	 */
	protected Rectangle computePopupBounds(int px,int py,int pw,int ph)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Rectangle screenBounds;

		// Calculate the desktop dimensions relative to the combo box.
		GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
		Point p = new Point();
		SwingUtilities.convertPointFromScreen(p,comboBox);
		if(gc != null)
		{
			Insets screenInsets = toolkit.getScreenInsets(gc);
			screenBounds = gc.getBounds();
			screenBounds.width -= (screenInsets.left + screenInsets.right);
			screenBounds.height -= (screenInsets.top + screenInsets.bottom);
			screenBounds.x += (p.x + screenInsets.left);
			screenBounds.y += (p.y + screenInsets.top);
		}
		else
			screenBounds = new Rectangle(p,toolkit.getScreenSize());

		Rectangle rect = new Rectangle(px,py,pw,ph);
		if(py + ph > screenBounds.y + screenBounds.height
		   && ph < screenBounds.height)
			rect.y = -rect.height;
		return rect;
	}

	/**
	 * Calculates the upper left location of the Popup.
	 */
	private Point getPopupLocation()
	{
		Dimension popupSize = comboBox.getSize();
		Insets insets = getInsets();

		// reduce the width of the scrollpane by the insets so that the popup
		// is the same width as the combo box.
		int iCalculatedWidth = 0;
		for(int iIndex = 0;iIndex < list.getColumnCount();iIndex++)
			iCalculatedWidth += list.getColumn(iIndex).getPreferredWidth();
		if(iCalculatedWidth > comboBox.getMaximumPopupWidth())
			iCalculatedWidth = comboBox.getMaximumPopupWidth();
		popupSize.setSize(popupSize.width - (insets.right + insets.left),
						  getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
		if(iCalculatedWidth > popupSize.width)
			popupSize.width = iCalculatedWidth;
		Rectangle popupBounds = computePopupBounds(0,comboBox.getBounds().height,
												   popupSize.width,popupSize.height);
		Dimension scrollSize = popupBounds.getSize();
		Point popupLocation = popupBounds.getLocation();

		scroller.setMaximumSize(scrollSize);
		scroller.setPreferredSize(scrollSize);
		scroller.setMinimumSize(scrollSize);

		list.revalidate();

		return popupLocation;
	}

	/**
	 * A utility method used by the event listeners.  Given a mouse event, it changes
	 * the list selection to the list item below the mouse.
	 */
	protected void updateListBoxSelectionForEvent(MouseEvent anEvent,boolean shouldScroll)
	{
		// XXX - only seems to be called from this class. shouldScroll flag is
		// never true
		Point location = anEvent.getPoint();
		if(list == null)
			return;
		int index = list.rowAtPoint(location);
		if(index == -1)
		{
			if(location.y < 0)
				index = 0;
			else
				index = comboBox.getModel().getSize() - 1;
		}
		if(list.getSelectedRow() != index)
			list.changeSelectedRow(index);
	}

	//
	// end Utility methods
	//=================================================================

	public void setVisible(boolean b)
	{
		if(!b)
		{
			comboBox.hideTip();
			if(comboBox.getPopupTable().isChildVisible() ||
			   comboBox.isFocusOwner())
				return;
		}
		super.setVisible(b);
	}
}
