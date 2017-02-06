package com.webdefault.devtools;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import tk.eatheat.floatingexample.FlyBitch;

/**
 * Created by orlandoleite on 2/6/17.
 */

public class DevToolsService extends FlyBitch implements View.OnClickListener
{
	private static final String LOG_TAG = "DevToolsService";
	
	private static final String FINNISH_DEV_TOOLS = "com.webdefault.DevTools";
	
	private BroadcastReceiver mBroadcastReceiver;
	
	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		if( mBroadcastReceiver == null )
		{
			mBroadcastReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive( Context context, Intent intent )
				{
					// Log.v( LOG_TAG, "onReceive: " + intent.getAction() );
					String actionName = intent.getAction();
					if( actionName.equals( FINNISH_DEV_TOOLS ) )
					{
						stopSelf();
					}
				}
			};
			
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction( FINNISH_DEV_TOOLS );
			
			Log.v( LOG_TAG, "registered receiver" );
			registerReceiver( mBroadcastReceiver, intentFilter );
		}
		
		return START_STICKY;
	}
	
	@Nullable
	@Override
	public IBinder onBind( Intent intent )
	{
		return null;
	}
	
	TextView ipText;
	EditText widthText, heightText, densityText;
	ToggleButton adbBtn;
	Button applyBtn, resetBtn;
	CheckBox usingRoot;
	
	@Override
	public View getView()
	{
		LayoutInflater inflater = LayoutInflater.from( this );
		
		View root = (View) inflater.inflate( R.layout.activity_main, null );
		
		root.findViewById( R.id.close_btn ).setOnClickListener( this );
		
		ipText = (TextView) root.findViewById( R.id.text_ip ); 
		
		widthText = (EditText) root.findViewById( R.id.text_width );
		heightText = (EditText) root.findViewById( R.id.text_height );
		densityText = (EditText) root.findViewById( R.id.text_density );
		
		adbBtn = (ToggleButton) root.findViewById( R.id.adb_tcp_btn );
		adbBtn.setOnClickListener( this );
		
		applyBtn = (Button) root.findViewById( R.id.apply_btn );
		applyBtn.setOnClickListener( this );
		
		resetBtn = (Button) root.findViewById( R.id.reset_btn );
		resetBtn.setOnClickListener( this );
		
		usingRoot = (CheckBox) root.findViewById( R.id.using_su_btn );
		
		updateValues();
		
		return root;
	}
	
	private void updateValues()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getRealMetrics( metrics );
		
		ipText.setText( getLocalIpAddress() + ":5555" );
		
		if( isPortrait() )
		{
			widthText.setText( metrics.widthPixels + "" );
			heightText.setText( metrics.heightPixels + "" );
		}
		else
		{
			widthText.setText( metrics.heightPixels + "" );
			heightText.setText( metrics.widthPixels + "" );
		}
		
		densityText.setText( metrics.densityDpi + "" );
		
		adbBtn.setChecked( false );
		
		try
		{
			Process process = Runtime.getRuntime().exec( "sh" );
			DataOutputStream outputStream = new DataOutputStream( process.getOutputStream() );
			
			outputStream.writeBytes( "getprop service.adb.tcp.port\n" );
			outputStream.flush();
			
			BufferedReader inputStream = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			
			String value = inputStream.readLine();
			
			if( value != null && value.equals( "5555" ) )
			{
				outputStream.writeBytes( "getprop init.svc.adbd\n" );
				outputStream.flush();
				
				value = inputStream.readLine();
				
				if( value != null && value.equals( "running" ) )
				{
					adbBtn.setChecked( true );
				}
			}
			
			outputStream.close();
			inputStream.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void toggleAdbd()
	{
		try
		{
			Process process = Runtime.getRuntime().exec( usingRoot.isChecked() ? "su" : "sh" );
			DataOutputStream outputStream = new DataOutputStream( process.getOutputStream() );
			
			// Log.v( LOG_TAG, "toggleAdbd " + adbBtn.isChecked() );
			if( adbBtn.isChecked() )
			{
				outputStream.writeBytes( "stop adbd\n" );
				outputStream.writeBytes( "setprop service.adb.tcp.port 5555\n" );
				outputStream.writeBytes( "start adbd\n" );
				outputStream.flush();
			}
			else
			{
				outputStream.writeBytes( "stop adbd\n" );
				outputStream.flush();
			}
			
			outputStream.close();
			
			updateValues();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void apply() throws Exception
	{
		try
		{
			Process process = Runtime.getRuntime().exec( usingRoot.isChecked() ? "su" : "sh" );
			DataOutputStream outputStream = new DataOutputStream( process.getOutputStream() );
			
			outputStream.writeBytes( "wm size " + widthText.getText() + "x" + heightText.getText() + "\n" );
			outputStream.writeBytes( "wm density " + densityText.getText() + "\n" );
			outputStream.flush();
			
			BufferedReader inputStream = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			
			String value = inputStream.readLine();
			
			if( value != null && !value.equals( "" ) )
				Toast.makeText( this, value, Toast.LENGTH_LONG ).show();
			
			process.waitFor();
			
			outputStream.close();
			inputStream.close();
			
			updateValues();
		}
		catch( IOException e )
		{
			throw new Exception( e );
		}
	}
	
	private void reset() throws Exception
	{
		try
		{
			Process process = Runtime.getRuntime().exec( usingRoot.isChecked() ? "su" : "sh" );
			DataOutputStream outputStream = new DataOutputStream( process.getOutputStream() );
			
			outputStream.writeBytes( "wm size reset\n" );
			outputStream.writeBytes( "wm density reset\n" );
			outputStream.flush();
			
			BufferedReader inputStream = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			
			String value = inputStream.readLine();
			
			if( value != null && !value.equals( "" ) )
				Toast.makeText( this, value, Toast.LENGTH_LONG ).show();
			
			process.waitFor();
			
			outputStream.close();
			inputStream.close();
			
			updateValues();
		}
		catch( IOException e )
		{
			e.printStackTrace();
			throw new Exception( e );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
			throw new Exception( e );
		}
	}
	
	private void close()
	{
		windowManager.removeView( root );
		root = null;
		
		stopSelf();
	}
	
	@Override
	public void onClick( View view )
	{
		try
		{
			switch( view.getId() )
			{
				case R.id.close_btn:
					close();
					break;
				
				
				case R.id.apply_btn:
					apply();
					break;
				
				case R.id.reset_btn:
					reset();
					break;
				
				case R.id.adb_tcp_btn:
					toggleAdbd();
					break;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			Toast.makeText( this, "A problem occurred.", Toast.LENGTH_LONG ).show();
		}
	}
	
	private boolean isPortrait()
	{
		WindowManager windowService = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
		int currentRatation = windowService.getDefaultDisplay().getRotation();
		
		if( Surface.ROTATION_0 == currentRatation || Surface.ROTATION_180 == currentRatation )
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	private static String getLocalIpAddress()
	{
		try
		{
			for( Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
			{
				NetworkInterface intf = en.nextElement();
				for( Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); )
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if( !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address )
					{
						return inetAddress.getHostAddress();
					}
				}
			}
		}
		catch( SocketException ex )
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		unregisterReceiver( mBroadcastReceiver );
	}
}
