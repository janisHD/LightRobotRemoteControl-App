package org.lightrobotremote;

import android.os.Handler;


/** Stores the current data values which will be sent to the robot.
 * 
 * @author Julian
 *
 */
public class LightRobotDataManager {
	
	
	/** Handles the communication with the Activiy
	 * 
	 */
	private Handler mHandler;
	
	/** sets the speed for the robot [-127, 127]
	 * positive values -> Forward
	 * negative values -> Backward
	 */
	private  byte mSpeed = 0;
	private static final byte MOVE_VALUE_MAX = 127;
	
	/** sets the direction (e.g. the turn rate) for the robot [-127, 127]
	 * positive values -> turns right, 128 turns on point
	 * negative values -> turns left, -127 turns on point
	 */
	private  byte mDirection = 0;//
	
	/** sets the color of the lamps [0, 255]
	 * b0, b1 -> brightness -> 00 off, 11 max
	 * b2, b3 -> red value
	 * b4, b5 -> green value
	 * b6, b7 -> blue value
	 */
	private  byte mColor = 0;
	private static final short MASK_SHORT_BYTE = 0xff;
	private static final byte MASK_BRIGHTNESS = 0x03;//0b00000011;
	private static final byte POSITION_BRIGHTNESS = 0;
	private static final byte MASK_RED = 0x0C;
	private static final byte POSITION_RED = 2;
	private static final byte MASK_GREEN = 0x30;
	private static final byte POSITION_GREEN = 4;
	private static final byte MASK_BLUE = -0x40;//0xc0 11000000
	private static final byte POSITION_BLUE = 6;
	private static final byte mColorLookup[] = {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,
												1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1, 1,1,1,1,
												2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,2,2,2,2,2,2, 2,2,2,2,
												3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3,3,3,3,3,3,3, 3,3,3,3};
	
	/** sets the mode of the robot AND the mode of the light
	 * b0, b1, b2, b3 -> Color mode -> 	0000 -> remote (color set by remote)
	 * 									0001 -> blink (color set by remote, blinks)
	 * 									0010 -> random (fades random lights)
	 * 									0011 -> random blink
	 * b4, b5, b6, b7 -> Driving mode ->	0000 -> remote
	 * 										0001 -> random drive
	 */
	private  byte mMode = 0;
	
	private static final byte POSITION_DRIVE_MODE = 0;
	private static final byte MASK_DRIVE_MODE = 0x03;
	
	public static final byte DRIVE_MODE_REMOTE = 0;
	public static final byte DRIVE_MODE_RANDOM = 1;
	
	private static final byte POSITION_COLOR_MODE = 4;
	private static final byte MASK_COLOR_MODE = -0x10;
	public static final byte COLOR_MODE_REMOTE = 0;
	public static final byte COLOR_MODE_BLINK = 1;
	public static final byte COLOR_MODE_RANDOM = 2;
	public static final byte COLOR_MODE_RANDOM_BLINK = 3;
	
	/** Data packet with the four fields [speed][direction][color][mode]
	 * 
	 */
	private  byte mDataPacket[] = {0, 0, 0, 0};
	private static final byte POSITION_PACKET_SPEED = 0;
	private static final byte POSITION_PACKET_DIRECTION = 1;
	private static final byte POSITION_PACKET_COLOR = 2;
	private static final byte POSITION_PACKET_MODE = 3;
	
	private  boolean mSendNewPacket = false;
	
	public LightRobotDataManager(Handler handler)
	{
		mHandler = handler;
	}
	
	
	public void resetMoveValues()
	{
		mSpeed = 0;
		mDirection = 0;
		updatePacket();
	}
	
	public void resetAllValues()
	{
		mSpeed = 0;
		mDirection = 0;
		mColor = 0;
		mMode = 0;
		updatePacket();
	}
	
	public void setSpeed(byte speed)
	{
		mSpeed = speed;
		updatePacket();
	}
	
	public void setDirection(byte direction)
	{
		mDirection = direction;
		updatePacket();
	}
	
	/** Changes the value of mSpeed not absolut, but relative according to the amount.
	 * 
	 * @param amount [-127, 127] the amount of altering the value, Overflow is checked (and not possible).
	 */
	public void alterSpeed(byte amount)
	{
		short delta = (short)(MOVE_VALUE_MAX - (mSpeed*Math.signum(amount)));
		if(delta > MOVE_VALUE_MAX)
			delta = MOVE_VALUE_MAX;
		if(delta < Math.abs(amount))
			amount = (byte)(delta * Math.signum(amount));
		mSpeed += amount;
		updatePacket();
	}
	
	/** Changes the value of mDirection not absolut, but relative according to the amount
	 * 
	 * @param amount [-127, 127] the amount of how much will the value be altered, Overflow is checked (and not possible).
	 */
	public void alterDirection(byte amount)
	{
		short delta = (short)(MOVE_VALUE_MAX - (mDirection*Math.signum(amount)));
		if(delta > MOVE_VALUE_MAX)
			delta = MOVE_VALUE_MAX;
		if(delta < Math.abs(amount))
			amount = (byte)(delta * Math.signum(amount));
		mDirection += amount;
		updatePacket();
	}
	
	public byte getSpeed()
	{
		return mSpeed;
	}
	
	public  byte getDirection() {
		return mDirection;
	}

	public void setColor(short brightness, short red, short green, short blue)
	{
		setColorBrightness(brightness);
		setColorRed(red);
		setColorGreen(green);
		setColorBlue(blue);
	}
	
	public void setColorBrightness(short brightness)
	{
		setColorPart(brightness, POSITION_BRIGHTNESS, MASK_BRIGHTNESS);
		updatePacket();
	}
	
	public void setColorRed(short red)
	{
		setColorPart(red, POSITION_RED, MASK_RED);
		updatePacket();
	}
	
	public void setColorGreen(short green)
	{
		setColorPart(green, POSITION_GREEN, MASK_GREEN);
		updatePacket();
	}
	
	public void setColorBlue(short blue)
	{
		setColorPart(blue, POSITION_BLUE, MASK_BLUE);
		updatePacket();
	}
	
	public  byte getColor() {
		return mColor;
	}
	
	public void setColorMode(byte color_mode)
	{
		byte tempMode = (byte)(mMode & ~MASK_COLOR_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(color_mode & MASK_COLOR_MODE)) << POSITION_COLOR_MODE);
		mMode = tempMode;
		updatePacket();
	}
	
	public void setMode(byte drive_mode)
	{
		byte tempMode = (byte)(mMode & ~MASK_DRIVE_MODE);//clear color mode field.
		tempMode |= (byte)(((byte)(drive_mode & MASK_DRIVE_MODE)) << POSITION_DRIVE_MODE);
		mMode = tempMode;
		updatePacket();
	}
	
	public byte getMode() {
		return mMode;
	}

	
	private void setColorPart(short value, byte position, byte mask)
	{
		byte tempValue = shortToColor(value, position);
		mColor = (byte)(tempValue | ((byte)(mColor & ~mask)));
	}
	
	private byte shortToColor(short value, byte position)
	{
		short tempValue = (byte)(value & MASK_SHORT_BYTE);//maximum value is now 254
		tempValue = mColorLookup[tempValue];//lookup which value should be used
		tempValue = (byte)(tempValue << position);//shift to correct position
		return (byte)tempValue;
	}
	
	private void updatePacket()
	{
		mDataPacket[POSITION_PACKET_SPEED] = mSpeed;
		mDataPacket[POSITION_PACKET_DIRECTION] = mDirection;
		mDataPacket[POSITION_PACKET_COLOR] = mColor;
		mDataPacket[POSITION_PACKET_MODE] = mMode;
		
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_SEND_DATA, 0, 0).sendToTarget();
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DISPLAY, 0, 0).sendToTarget();
	}
	
	public byte[] getDataPacket()
	{
		return mDataPacket;
	}
	

}
