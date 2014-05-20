package com.fss.swing;

import java.io.*;
import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: DDTP</p>
 * <p>Description: Package of parameter can be transfer on net</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class HistoryModelGroup
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public static String HISTORY_DIR = Global.CONFIG_DIR + "history/";
	private static int MAX_ELEMENT = 4096;
	public Hashtable models;
	public boolean modified;
	public File history;
	public long historyModTime;
	public String name;
	////////////////////////////////////////////////////////
	// Static initialize
	////////////////////////////////////////////////////////
	static
	{
		try
		{
			FileUtil.forceFolderExist(HISTORY_DIR);
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param name String
	 */
	////////////////////////////////////////////////////////
	public HistoryModelGroup(String name)
	{
		if(name == null || name.length() == 0)
			throw new IllegalArgumentException("name can not bel null");
		this.name = name;
		loadHistory();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void loadHistory()
	{
		if(models == null)
			models = new Hashtable();
		history = new File(HISTORY_DIR + name);
		if(!history.exists())
			return;
		historyModTime = history.lastModified();

		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new FileReader(history));

			HistoryModel currentModel = null;
			String line;

			while(models.size() < MAX_ELEMENT && (line = in.readLine()) != null)
			{
				if(line.startsWith("[") && line.endsWith("]"))
				{
					if(currentModel != null)
						models.put(currentModel.getName(),currentModel);
					String modelName = StringEscapeUtil.unescapeJava(line.substring(1,line.length() - 1));
					currentModel = new HistoryModel(modelName,this);
				}
				else if(currentModel == null)
					throw new IOException("History data starts before model name");
				else
					currentModel.data.addElement(StringEscapeUtil.unescapeJava(line));
			}

			if(currentModel != null)
				models.put(currentModel.getName(),currentModel);
		}
		catch(FileNotFoundException fnf)
		{
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch(IOException io)
			{
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void saveHistory()
	{
		javax.swing.plaf.metal.MetalComboBoxUI a;
		if(!modified)
			return;
		File file1 = new File(HISTORY_DIR + name + ".tmp");
		File file2 = new File(HISTORY_DIR + name);
		if(file2.exists() && file2.lastModified() != historyModTime)
			return;

		String lineSep = System.getProperty("line.separator");
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(new FileWriter(file1));
			if(models != null)
			{
				Enumeration modelEnum = models.elements();
				while(modelEnum.hasMoreElements())
				{
					HistoryModel model = (HistoryModel)modelEnum.nextElement();
					if(model.getSize() == 0)
						continue;

					out.write('[');
					out.write(StringEscapeUtil.escapeJava(model.getName()));
					out.write(']');
					out.write(lineSep);

					for(int i = 0;i < model.getSize();i++)
					{
						out.write(StringEscapeUtil.escapeJava(model.getItem(i)));
						out.write(lineSep);
					}
				}
			}
			out.close();

			file2.delete();
			file1.renameTo(file2);
			modified = false;
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		finally
		{
			try
			{
				if(out != null)
					out.close();
			}
			catch(IOException e)
			{
			}
		}
		historyModTime = file2.lastModified();
	}
}
