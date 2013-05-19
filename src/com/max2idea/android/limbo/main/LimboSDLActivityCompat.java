package com.max2idea.android.limbo.main;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;

import android.view.View;

public class LimboSDLActivityCompat extends SDLActivity 
{
	@Override
	public SDLSurface getSDLSurface() {
		// TODO Auto-generated method stub
		mSurface = new LimboSDLSurfaceCompat(getApplication());
		
		
		return mSurface;
	}
	
	

}
