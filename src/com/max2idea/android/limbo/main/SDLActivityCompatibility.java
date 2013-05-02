package com.max2idea.android.limbo.main;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;

import android.view.View;

public class SDLActivityCompatibility extends SDLActivity 
{
	@Override
	public SDLSurface getSDLSurface() {
		// TODO Auto-generated method stub
		mSurface = new SDLSurfaceCompatibility(getApplication());
		
		
		return mSurface;
	}
	
	

}
