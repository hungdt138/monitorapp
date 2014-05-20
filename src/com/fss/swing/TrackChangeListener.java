package com.fss.swing;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FPT - FSS</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface TrackChangeListener
{
	void backup();
	void restore();
	boolean isChanged();
	void clearBackup();
}
