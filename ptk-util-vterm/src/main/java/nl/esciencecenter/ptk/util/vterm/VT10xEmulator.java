/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.util.vterm;

import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_ESC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.ui.charpane.ICharacterTerminal;
import nl.esciencecenter.ptk.ui.charpane.StyleChar;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token;

/**
 * 
 * Implementation of most VT100 codes.
 * With some vt102, and starting with xterm. 
 * 
 * @author Piter T. de Boer
 */
public class VT10xEmulator extends Emulator
{
    static ClassLogger logger; 
    
    static
    {
        logger=ClassLogger.getLogger(VT10xEmulator.class);
        // logger.setLevelToDebug(); 
    }
    
   //==========================================================================
   //
   //==========================================================================

    String encoding="UTF-8"; 
    
    String termTytpe=VT10xEmulatorDefs.TERM_XTERM; 

    // === State Parameters === /// 
    
    /**  Inclusive region start: (y>=y1)*/
    int region_y1 = 0; // 1;
    
    /**  EXCLUSIVE region end:(y<y2) */ 
    int region_y2 = 0; //term_height;
    
    int tabSize=8;
   
    private boolean applicationCursorKeys;
	
    private int savedCursorX;
	
    private int savedCursorY;

	private boolean hasRegion;
    
    /** 
     * Construct new Terminal Emulator. 
     * 
     * @param term
     * @param in
     * @param out
     */
    public VT10xEmulator(ICharacterTerminal term, InputStream in,OutputStream out)
    {
    	super(term, in,out);
    }
	
	public void sendSize(int cols, int rows) 
	{
		this.nr_columns=cols;
		this.nr_rows=rows; 
		this.region_y1=0;
		this.region_y2=rows; 
		//sendTermSize(); 
	}
   
    public void sendTermSize() 
    {
        byte bytes[]=
        {
            (byte)CTRL_ESC,
            '8',
            ';',
            (byte)('0'+nr_rows/100), 
            (byte)('0'+(nr_rows/10)%10),
            (byte)('0'+nr_rows%10),
            ';',
            (byte)('0'+nr_columns/100),
            (byte)('0'+(nr_columns/10)%10),
            (byte)('0'+nr_columns%10),
            't'
        };
        
        try
        {
            this.send(bytes);
        }
        catch (IOException e)
        {
            System.out.println("***Error: Exception:"+e); 
            e.printStackTrace();
        }

    }
    
    public void sendTermType() 
    {
        byte bytes[]=
        {
            (byte)CTRL_ESC,
            '[',
            (byte)('0'), // VT100
            ';',
            (byte)('0'), // version 0.1.0.0
            (byte)('1'), // version 0.1.0.0
            (byte)('0'), // version 0.1.0.0
            (byte)('0'), // version 0.1.0.0
            
            ';',
            (byte)('0'), //
            'c'
        };
        
        try
        {
            this.send(bytes);
        }
        catch (IOException e)
        {
        	checkIOException(e,true); 
        }

    }

    protected void checkIOException(Exception e, boolean sendException)
    {
    	System.err.println("***Error:"+(sendException?"SEND":"RECEIVE")+"Exception:"+e); 
    	e.printStackTrace();
    }
    
   
	
    public void start()
    {
        nr_columns = term.getColumnCount();
        nr_rows = term.getRowCount();
        region_y1=0; 
        region_y2=nr_rows;
        
        setConnected(true); 
     
        while (signalTerminate==false)
        {
        	synchronized(haltMutex)
        	{
        		if (this.signalHalt)
        		{
					try 
					{	
						this.haltMutex.wait();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					} 
        		}
        	}
        	
        	
        	try
        	{
        		nextToken(); 
        	}
        	// Catch ALL and continue!!
        	catch (Throwable e)
        	{
        	    logger.logException(ClassLogger.ERROR,e,"nextToken()\n");
              //break ;
        	}
        }// while
        
        logger.infoPrintf("*** Session Ended ***\n");
        setConnected(false);    
    }

    protected void nextToken() throws IOException
    {    	  
    	readErrorStream(); 

    	int x = term.getCursorX(); 
    	int y = term.getCursorY(); 

    	Token token=tokenizer.nextToken();

    	// Text representation parse bytes sequence
    	byte bytes[]=tokenizer.getBytes();
    	
    	//logger.debugPrintf("-> New Token="+this.tokenizer.formattedBytesString(bytes)+" cursor="+x+","+y); 


    	int arg1=0; 
    	int arg2=0; 

    	int numIntegers=tokenizer.getNumIntegers(); 

    	if (numIntegers>0)
    		arg1=tokenizer.getInteger(0);

    	if (numIntegers>1)
    		arg2=tokenizer.getInteger(1); 
    	
    	int defNum1=1; 
    	if (arg1>0)
    		defNum1=arg1;
    	
    	switch(token)
    	{
    	case EOF:
            logger.debugPrintf("EOF: Connection Closed.\n"); 
    		signalTerminate=true;
    		break;
    	case EOT:
            logger.debugPrintf("EOT: Connection Closed.\n"); 
    		signalTerminate=true;
    		break; 
    	case NUL: 
    	case DEL:
    		//ignore
    		break;
    	case REQ_IDENTIFY:
    		// supported ?
            logger.warnPrintf("Warning: Request Identify not tested ***\n");
    		this.sendTermType(); 
    		break;
    	case BEEP: 
    		term.beep();
    		break;
    	case HT: 
    	{   // TAB
    		x = ((x  / tabSize + 1) * tabSize);
    		if (x >= nr_columns)
    		{
    			x = 0;
    			y += 1;
    		}
    		term.setCursor(x, y);
    		//term.drawCursor();
    		break; 
    	}
    	case BS:
    	{   // backspace
    		x -= 1;
    		if (x < 0)
    		{
    			y -= 1;
    			x = nr_columns - 1;
    		}
    		term.setCursor(x, y);
    		break; 
    	}
    	case LF:
    	case VT:
    	case FF: 
    	{
    		// MIN(nr_rows,region); 
    		int maxy=nr_rows; 
    		if (region_y2<maxy)
    			maxy=region_y2; 

    		// Auto LineFeed when y goes out of bounds (or region) 
    		if (y+1>=maxy)
    		{
    			// scroll REGION 
    			term.scrollRegion(this.region_y1,maxy,1,true);
    			y=maxy-1; // explicit keep cursor in region. 
    		}
    		else
    		{
    			y += 1;
    			term.setCursor(x, y);
    			logger.debugPrintf("FF: New Cursor (x,y)=%s,%s\n",x,y);
    		}
    		break;
    	}
    	case CR:
    	{   // carriage return 
    		x = 0;
    		term.setCursor(x, y);

    		break; 
    	}
    	case UP:
            logger.debugPrintf("UP:%s\n",token,arg1);  
    		term.setCursor(x,y); 
    		break; 
    	case DOWN:
            logger.debugPrintf("DOWN:%s\n",token,arg1);  
    		y+=defNum1; 
    		term.setCursor(x,y); 
    		break; 
    	case LEFT:
            logger.debugPrintf("LEFT:%s\n",token,arg1);  
    		x-=defNum1;
    		term.setCursor(x,y); 
    		break; 
    	case RIGHT:
            logger.debugPrintf("RIGHT:%s\n",token,arg1);  
    		x+=defNum1;
    		term.setCursor(x,y); 
    		break; 
    	case SET_CURSORX:
    		if (numIntegers>0)
    			x=arg1-1; 
    		else
    			x=0; 
    		term.setCursor(x,y); 
    		break; 
    	case SAVE_CURSOR:
    		saveCursor(); 
    		break; 
    	case RESTORE_CURSOR:
    		restoreCursor(); 
    		break; 
    	case SET_REGION:
    	{
    		if (numIntegers==0)
    		{
    			//reset
    			region_y1=0; 
    			region_y2=nr_rows;
    			hasRegion=false;
    		}
    		else
    		{
    			region_y1=arg1-1; // inclusive ->inclusive (-1)  
    			region_y2=arg2; // inclusive -> exclusive (-1+1)
    			hasRegion=true; 
    		}
    		break;
    	}
    	case SET_COLUMN:
    	{
    		term.setCursor(arg1-1,y);
    		break; 
    	}
    	case SET_ROW:
    	{
    		term.setCursor(x,arg1-1);
    		break; 
    	}
    	case DEL_CHAR:
    	{
    		// delete under cursor shift right of cursor left ! 
    		int num=1; 
    		if (numIntegers>0)
    			num=arg1; 
    		// mutli delete is move chars to left
    		term.move(x+num,y,nr_columns-x-num,1,x,y); 
    		break; 
    	}
    	case ERASE_CHARS:
    	{
    		int n=arg1; 
    		for (int i=0;i<n;i++)
    			term.putChar(' ', x+i, y);
    		term.setCursor(x,y);
    		break; 
    	}
    	case DELETE_LINES:
    	{
    		int n=arg1; 
    		for (int i=0;i<n;i++)
    			for (int j=0;j<nr_columns-1;j++)
    				term.putChar(' ', j,y+i);
    		term.setCursor(x,y);
    		break; 
    	}
    	case INDEX:
    	{  // move down
    		if (y+1>=this.region_y2)
    		{
    			// move down scrollRegion up: 
    			term.scrollRegion(this.region_y1,this.region_y2,1,true);
    		}
    		else
    		{
    			y++;
    			term.setCursor(x, y);
    		}
    		break;  
    	}
    	case NEXT_LINE:
    	{  // move down
    		if (y+1>=this.region_y2)
    		{
    			// move down scrollRegion up: 
    			term.scrollRegion(this.region_y1,this.region_y2,1,true);
    			term.setCursor(0, y);
    		}
    		else
    		{
    			y++;
    			term.setCursor(0, y);
    		}
    		break;  
    	}
    	case REVERSE_INDEX: 
    	{   // move up 
    		if ((y-1)<this.region_y1)
    		{
    			// move up scrollRegion down: 
    			term.scrollRegion(this.region_y1,this.region_y2,1,false);
    		}
    		else
    		{
    			y--;
    			term.setCursor(x, y);
    		}
    		break;  
    	}
    	case INSERT_LINES: 
    	{   
    		//default: one
    		int numlines=1;

    		if (arg1>0) 
    			numlines=arg1+1; 

    		// insert at current position: scroll down:
    		term.scrollRegion(y,this.region_y2,numlines,false);
    		break;  
    	}

    	case SET_CURSOR:
    	{
    		if (numIntegers>0)
    			y = arg1-1;
    		else
    			y=0; 

    		if (numIntegers>1) 
    			x = arg2-1;
    		else
    			x=0; 

    		logger.debugPrintf("SET_CURSOR:%d,%d\n",x,y);

    		term.setCursor(x, y);
    		break; 
    	}
    	case LINE_ERASE:
    	{
    		int mode=0; 
    		if (numIntegers>0)
    			mode=arg1; 

    		if (mode==0) 
    		{
    			// cursor(inclusive) to end of line 
    			term.clearArea(x,y,nr_columns,y+1); 
    		}
    		else if (mode==1) 
    		{
    			// begin of line to cursor (inclusive)  
    			term.clearArea(0,y,x+1,y+1); 
    		}
    		else if (mode==2)
    		{
    			// complete line 
    			term.clearArea(0,y,nr_columns,y+1); 
    		}
    		break; 
    	} 
    	case SCREEN_ERASE:
    	{
    		int mode=2; // no arg = full screen ? (VI does this!)
    		if (numIntegers>0)
    			mode=arg1; 

    		if (mode==0) 
    		{
    			// cursor(inclusive) to end screen 
    			term.clearArea(x,y,nr_columns,y); // rest of line 
    			term.clearArea(0,y+1,nr_columns,nr_rows); 
    		}
    		else if (mode==1) 
    		{
    			// begin of screen to cursor (inclusive)  
    			term.clearArea(0,0,nr_columns,y);  
    			term.clearArea(0,y,x+1,y);  
    		}
    		else if (mode==2)
    		{
    			// complete screen 
    			term.clearArea(0,0,nr_columns,nr_rows); 
    			term.setCursor(0,0); //reset cursor ? 
    		}
    		break; 
    	}  
    	case SET_FONT_STYLE:
    		handleSetFontStyle(term,tokenizer.getNumIntegers(),tokenizer.getIntegers()); 
    		break; 
    	case DEC_SETMODE:
    	case DEC_RESETMODE:
    		boolean value=(token.compareTo(Token.DEC_SETMODE)==0);
       		handleDecMode(term,tokenizer.getNumIntegers(),tokenizer.getIntegers(),value); 
    		break; 
    	case DEVICE_STATUS:
    	{
    		if (arg1==6)
    		{
    		    // debug(">>> Request Cursor Report");
    			x=120;
    			y=30;

    			byte px1=(byte)('0'+(x/10)%10);
    			byte px2=(byte)('0'+x%10); 
    			byte py1=(byte)('0'+(y/10)%10); 
    			byte py2=(byte)('0'+(y%10));

    			byte sbytes[]={(byte)CTRL_ESC,'[',py1,py2,';',px1,px2,'R'};

    			this.send(sbytes); 
    		}
    		else
    		{
    		    logger.warnPrintf("DEVICE_STATUS: Unkown device status mode:%d\n",arg1); 
    		}
    		break;
    	}
    	case CHARSET_GO_UK:
    		term.setCharSet(0,ICharacterTerminal.VT_CHARSET_UK);
    		break;
    	case CHARSET_G1_UK:
    		term.setCharSet(1,ICharacterTerminal.VT_CHARSET_UK);
    		break;
    	case CHARSET_GO_US:
    		term.setCharSet(0,ICharacterTerminal.VT_CHARSET_US);
    		break;
    	case CHARSET_G1_US:
    		term.setCharSet(1,ICharacterTerminal.VT_CHARSET_US);
    		break;
    	case CHARSET_GO_GRAPHICS:
    		term.setCharSet(0,ICharacterTerminal.VT_CHARSET_GRAPHICS); 
    		break;
    	case CHARSET_G1_GRAPHICS:
    		term.setCharSet(1,ICharacterTerminal.VT_CHARSET_GRAPHICS); 
    		break;
    	case CHARSET_G0:
    		term.setCharSet(0);
    		break;
    	case CHARSET_G1:
    		term.setCharSet(1); 
    		break;
    	case CHAR: 
    		// one or more characters: moves cursor !
    		writeChar(bytes);
    	break; 
    	case XGRAPHMODE:
    	{
    		// Graph mode 
    		// 1 short title
    		// 2 long title 
    		int type=arg1;
    		this.fireGraphModeEvent(type,tokenizer.getStringArgument()); 	
    		//System.err.println("XGRAPH type="+type+"np="+token.np+","+token.nd+"-"+token.strArg); 
    		break; 
    	}

    	case REQ_UNKNOWN:
    	    fixmePrintf("Unkown request:%s\n",token);
    		break; 
    	case REQ_IDENTIFY2:
    		if (this.tokenizer.getNumIntegers()>0)
    		{
    			fixmePrintf("IDENTIFY mode=%s",arg1); 
    		}
    		sendTermType();
    		break; 
    	case UNKNOWN:
    	case ERROR:
    		String seqstr=tokenizer.formattedBytesString(bytes);
    		// vt100 speficyfies to write checkerboard char: 
    		// drawChar('▒');
    		fixmePrintf("Token error:%s:%s,sequence=%s\n",token,tokenizer.getText(encoding+":"),seqstr);
    		break; 
    		// rest: 
    	case ETX:
    	case ENQ: 
    	case DC1: 
    	case DC2:
    	default:
    		fixmePrintf("Unimplementation Token:%s,args=%s\n",token,tokenizer.getFormattedArguments()); 
    	break; 
    	}// switch (token) 
    }
    
    private void handleSetFontStyle(ICharacterTerminal charTerm, int numIntegers,
			int[] integers) 
    {
    	int mode=0; 

		if (numIntegers == 0)
		{
			// empty = clear
			charTerm.setDrawStyle(0);  
		}
		else
		{
			mode=integers[numIntegers]; 
			// Aboscure undocumented feature ? 
			// 38 ; 5 ; Ps 	Set background color to Ps
			// 48 ; 5 ; Ps 	Set foreground color to Ps
			//
			if (((mode==38) || (mode==48)) && (numIntegers==3))
			{
				int ccode=tokenizer.getInteger(2); 
				fixmePrintf("Got multi color code:%s\n",ccode);

				if (mode==38)
				{
					charTerm.setDrawBackground(ccode); 
				}
				else if (mode==48)
				{
					charTerm.setDrawForeground(ccode); 
				}

			}
			else
				for (int i=0;i<numIntegers;i++)
				{
					mode=tokenizer.getInteger(i); 

					if (mode==0)
						charTerm.setDrawStyle(0);                        			// reset
						else if (mode == 1)
							charTerm.addDrawStyle(StyleChar.STYLE_BOLD); 
						else if (mode == 4)
							charTerm.addDrawStyle(StyleChar.STYLE_UNDERSCORE); 
						else if (mode == 5)
						{
							// blink supported ? 
									charTerm.addDrawStyle(StyleChar.STYLE_BLINK);
									charTerm.addDrawStyle(StyleChar.STYLE_UBERBOLD);

						}
						else if (mode == 7)
							charTerm.addDrawStyle(StyleChar.STYLE_INVERSE);
						else if (mode == 8)
							charTerm.addDrawStyle(StyleChar.STYLE_HIDDEN); 

						else if((mode>=30) && (mode<=37))
							charTerm.setDrawForeground(mode-30);  
						else if((mode>=40) && (mode<=47))
							charTerm.setDrawBackground(mode-40);  
						else if (mode==39)
							charTerm.setDrawBackground(-1); 
						else if (mode==49)
							charTerm.setDrawForeground(-1); 

				}
		}
	}
    
    private void handleDecMode(ICharacterTerminal charTerm, int numIntegers,
    		int[] integers,boolean value) 
   
    {
    	if (numIntegers==0)
    		return ; //Reset all ? 
    	
    	int mode=integers[0]; 
    		
    	switch (mode)
    	{
    		case 1:
    			this.applicationCursorKeys=value;
    			break; 
    		case 3: 
    		{
    			if (value)
    				this.nr_columns=132;
    			else
    				this.nr_columns=80; 
    			charTerm.setColumns(nr_columns); 
    			this.fireResizedEvent(nr_columns,nr_rows);
    			break; 
    		}
    		case 4:
    			charTerm.setSlowScroll(value);
    			break; 
    		case 7: 	
    			logger.debugPrintf("DECMODE:wraparound=%d\n",value);
    			charTerm.setWrapAround(value);
    			break;
    		case 25:
    			charTerm.setEnableCursor(value);
    			break; 
    		case 45:
    		    logger.warnPrintf("Received unsupported DECMODE:Set Alt Screen=%d\n",value);
    			boolean result=charTerm.setAltScreenBuffer(value);
    			if ((value==true) && (result==false))
    				fixmePrintf("Warning Alternative Text Buffer not supported\n"); 
    			break; 
    		case 1048: 
    			if (value)
    				saveCursor(); 
    			else
    				restoreCursor(); 
    			break; 
    		case 1049:
    		{
    			// switch to als screen + use application cursor keys
    			if (value)
    				saveCursor(); 
    			else
    				restoreCursor(); 
    			this.applicationCursorKeys=value;
    			charTerm.setAltScreenBuffer(value);
    			if (value)
    				charTerm.clearText(); 
    			break;
    		}
    		default:
    			fixmePrintf("Unkown DEC mode=%d\n",mode); 
    		break;
    		}
   		}
   		
	private void restoreCursor() 
    {
    	this.term.setCursor(savedCursorX,savedCursorY); 
	}

	private void saveCursor() 
	{
		savedCursorX=term.getCursorX();
		savedCursorY=term.getCursorY();
	}

	//byte[] singleChar=new byte[1]; 
    //byte[] wChar=new byte[2]; 
    

    private void writeChar(byte[] bytes)
    {
    	// let terminal do auto wrap around. 
    	term.writeChar(bytes); 
    	
        // update cursor 
        //int x=term.getCursorX(); 
        //int y=term.getCursorY();
        //term.putChar(bytes, x, y);
        // increment: let terminal do auto wrap around. 
        //term.moveCursor(1,0); 
        
    }
  
    public byte[] getKeyCode(String keystr) 
    {
    	return VT10xEmulatorDefs.getKeyCode(termType,keystr);
	}

    @Override
    public Object[][] getTokenTable()
    {
        return VT10xEmulatorDefs.getTokenTable(); 
    }
  
    private void fixmePrintf(String format,Object... args)
    {
        logger.warnPrintf("FIXME:"+format, args); 
    }
   
}
 
   

    

