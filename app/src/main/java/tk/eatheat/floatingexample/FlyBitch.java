package tk.eatheat.floatingexample;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public abstract class FlyBitch extends Service
{
	
	
	protected WindowManager windowManager;
	protected View root;
	
	@Override
	public IBinder onBind( Intent intent )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public abstract View getView();
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		windowManager = (WindowManager) getSystemService( WINDOW_SERVICE );
		
		root = getView();
		
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT );
		
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;
		
		windowManager.addView( root, params );
		
		try
		{
			root.setOnTouchListener( new View.OnTouchListener()
			{
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;
				
				@Override
				public boolean onTouch( View v, MotionEvent event )
				{
					switch( event.getAction() )
					{
						case MotionEvent.ACTION_DOWN:
							
							// Get current time in nano seconds.
							
							initialX = paramsF.x;
							initialY = paramsF.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							paramsF.x = initialX + (int) ( event.getRawX() - initialTouchX );
							paramsF.y = initialY + (int) ( event.getRawY() - initialTouchY );
							windowManager.updateViewLayout( root, paramsF );
							break;
					}
					return false;
				}
			} );
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if( root != null ) windowManager.removeView( root );
	}
	
}