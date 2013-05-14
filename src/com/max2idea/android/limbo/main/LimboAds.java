package com.max2idea.android.limbo.main;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.widget.Toast;

//import com.max2idea.android.limbo.mainarmv7.R;
import com.max2idea.android.limbo.utils.FileUtils;
import com.max2idea.android.limbo.mainarmv7.R;

import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.OnAdLoadedListener;
public class LimboAds {

	public static LimboAdView mAdView;
	public static void setupAds() {
		mAdView = (LimboAdView) LimboActivity.activity.findViewById(R.id.adview);
		FileUtils fileutils = new FileUtils();
		String adunit = "";
		try {
			adunit = fileutils.LoadFile(LimboActivity.activity, "ADUNIT.ADS", false);
		} catch (IOException ex) {
			Logger.getLogger(LimboActivity.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		mAdView.setAdUnitId(adunit); // Enter your Ad Unit ID from www.mopub.com
		mAdView.loadAd();

		mAdView.setOnAdLoadedListener(new OnAdLoadedListener() {
			public void OnAdLoaded(MoPubView mpv) {
//				 Toast.makeText(LimboActivity.activity.getApplicationContext(),
//				 "Ad successfully loaded.", Toast.LENGTH_SHORT).show();
			}
		});

	}
}
