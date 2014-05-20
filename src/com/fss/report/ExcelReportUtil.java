package com.fss.report;

import jxl.write.*;
import jxl.format.*;
import com.fss.util.*;

public class ExcelReportUtil
{
	public static int miStyleCount = 2;
	public WritableCellFormat TITLE_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat HEADER_ROW[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat HEADER_ROW_CENTER[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat HEADER_COL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat HEADER_COL_LEFT[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat TOTAL_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat LABEL_CELL[] = new WritableCellFormat[miStyleCount];

	public WritableCellFormat STRING_DATA_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat INTEGER_DATA_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat FLOAT_DATA_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat DATE_DATA_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat TIME_DATA_CELL[] = new WritableCellFormat[miStyleCount];
	public WritableCellFormat DATE_TIME_DATA_CELL[] = new WritableCellFormat[miStyleCount];

	public ExcelReportUtil()
	{
		try
		{
			WritableFont fntData = new WritableFont(WritableFont.TAHOMA,10,WritableFont.NO_BOLD,false);
			WritableFont fntHeader = new WritableFont(WritableFont.TAHOMA,10,WritableFont.BOLD,false);
			WritableFont fntTitle = new WritableFont(WritableFont.TAHOMA,12,WritableFont.BOLD,false);
			fntTitle.setColour(jxl.format.Colour.OCEAN_BLUE);
			WritableFont fntLabel = new WritableFont(WritableFont.TAHOMA,10,WritableFont.BOLD,false);
			NumberFormat fmtInteger = new NumberFormat("#,##0");
			NumberFormat fmtFloat = new NumberFormat("#,##0.00");
			DateFormat fmtDate = new DateFormat("dd/mm/yyyy");
			DateFormat fmtTime = new DateFormat("hh:mm:ss");
			DateFormat fmtDateTime = new DateFormat("dd/mm/yyyy hh:mm:ss");

			for(int iIndex = 0;iIndex < miStyleCount;iIndex++)
			{
				TITLE_CELL[iIndex] = new WritableCellFormat(fntTitle);
				HEADER_ROW[iIndex] = new WritableCellFormat(fntHeader);
				HEADER_ROW_CENTER[iIndex] = new WritableCellFormat(fntHeader);
				HEADER_COL[iIndex] = new WritableCellFormat(fntHeader);
				HEADER_COL_LEFT[iIndex] = new WritableCellFormat(fntHeader);
				LABEL_CELL[iIndex] = new WritableCellFormat(fntLabel);
				TOTAL_CELL[iIndex] = new WritableCellFormat(fntHeader,fmtInteger);

				STRING_DATA_CELL[iIndex] = new WritableCellFormat(fntData);
				INTEGER_DATA_CELL[iIndex] = new WritableCellFormat(fntData,fmtInteger);
				FLOAT_DATA_CELL[iIndex] = new WritableCellFormat(fntData,fmtFloat);
				DATE_DATA_CELL[iIndex] = new WritableCellFormat(fntData,fmtDate);
				TIME_DATA_CELL[iIndex] = new WritableCellFormat(fntData,fmtTime);
				DATE_TIME_DATA_CELL[iIndex] = new WritableCellFormat(fntData,fmtDateTime);

				HEADER_ROW[iIndex].setWrap(true);
				HEADER_ROW_CENTER[iIndex].setWrap(true);
				HEADER_COL[iIndex].setWrap(true);
				HEADER_COL_LEFT[iIndex].setWrap(true);
				TOTAL_CELL[iIndex].setWrap(true);
				LABEL_CELL[iIndex].setWrap(true);
				STRING_DATA_CELL[iIndex].setWrap(true);
				INTEGER_DATA_CELL[iIndex].setWrap(true);
				FLOAT_DATA_CELL[iIndex].setWrap(true);
				DATE_DATA_CELL[iIndex].setWrap(true);
				TIME_DATA_CELL[iIndex].setWrap(true);
				DATE_TIME_DATA_CELL[iIndex].setWrap(true);

				HEADER_ROW[iIndex].setBackground(jxl.format.Colour.LIGHT_GREEN);
				HEADER_ROW_CENTER[iIndex].setBackground(jxl.format.Colour.LIGHT_GREEN);
				HEADER_COL[iIndex].setBackground(jxl.format.Colour.PALE_BLUE);
				HEADER_COL_LEFT[iIndex].setBackground(jxl.format.Colour.PALE_BLUE);
				TOTAL_CELL[iIndex].setBackground(jxl.format.Colour.PALE_BLUE);

				TITLE_CELL[iIndex].setAlignment(jxl.format.Alignment.CENTRE);
				TOTAL_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);
				HEADER_COL[iIndex].setAlignment(jxl.format.Alignment.CENTRE);
				HEADER_COL_LEFT[iIndex].setAlignment(jxl.format.Alignment.LEFT);
				HEADER_ROW[iIndex].setAlignment(jxl.format.Alignment.LEFT);
				HEADER_ROW_CENTER[iIndex].setAlignment(jxl.format.Alignment.CENTRE);
				LABEL_CELL[iIndex].setAlignment(jxl.format.Alignment.CENTRE);

				TITLE_CELL[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				TOTAL_CELL[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				HEADER_COL[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				HEADER_COL_LEFT[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				HEADER_ROW[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				HEADER_ROW_CENTER[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);
				LABEL_CELL[iIndex].setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE.CENTRE);

				INTEGER_DATA_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);
				FLOAT_DATA_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);
				DATE_DATA_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);
				TIME_DATA_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);
				DATE_TIME_DATA_CELL[iIndex].setAlignment(jxl.format.Alignment.RIGHT);

				HEADER_ROW[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				HEADER_ROW_CENTER[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				HEADER_COL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				HEADER_COL_LEFT[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				TOTAL_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				STRING_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				INTEGER_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				FLOAT_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				DATE_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				TIME_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				DATE_TIME_DATA_CELL[iIndex].setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
			}
			TOTAL_CELL[1].setAlignment(jxl.format.Alignment.LEFT);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void deleteOldFile(String strPath)
	{
		FileUtil.deleteOldFile(strPath,"*.xls",3600000);
	}
}
