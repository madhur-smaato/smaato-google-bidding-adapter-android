package com.smaato.sdk.adapters.google;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.smaato.sdk.core.ad.AdRequestParams;
import com.smaato.sdk.interstitial.EventListener;
import com.smaato.sdk.interstitial.Interstitial;
import com.smaato.sdk.interstitial.InterstitialAd;
import com.smaato.sdk.interstitial.InterstitialError;
import com.smaato.sdk.interstitial.InterstitialRequestError;

public class SmaatoInterstitialAdRenderer extends BaseAdRenderer implements MediationInterstitialAd {
    private final MediationInterstitialAdConfiguration adConfiguration;
    private final MediationInterstitialAdCallback adLoadCallback;
    private EventListener interstitialEventListener;
    private InterstitialAd smaatoInterstitialAd;

    public SmaatoInterstitialAdRenderer(
            MediationInterstitialAdConfiguration adConfiguration,
            MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adLoadCallback) {
        this.adConfiguration = adConfiguration;
        this.adLoadCallback = adLoadCallback.onSuccess(SmaatoInterstitialAdRenderer.this);;
    }

    void render() {
        if (interstitialEventListener == null) {
            interstitialEventListener = new EventListener() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    smaatoInterstitialAd = interstitialAd;
                }

                @Override
                public void onAdFailedToLoad(@NonNull InterstitialRequestError interstitialRequestError) {
                    adLoadCallback.onAdFailedToShow(new AdError(1, interstitialRequestError.getInterstitialError().name(),
                            "com.smaato.sdk.adapters.google"));
                }

                @Override
                public void onAdError(@NonNull InterstitialAd interstitialAd, @NonNull InterstitialError interstitialError) {
                    adLoadCallback.onAdFailedToShow(new AdError(1,interstitialError.name(), "com.smaato.sdk.adapters.google"));
                }

                @Override
                public void onAdOpened(@NonNull InterstitialAd interstitialAd) {
                    adLoadCallback.onAdOpened();
                }

                @Override
                public void onAdClosed(@NonNull InterstitialAd interstitialAd) {
                    adLoadCallback.onAdClosed();
                }

                @Override
                public void onAdClicked(@NonNull InterstitialAd interstitialAd) {
                    adLoadCallback.onAdOpened();
                    adLoadCallback.reportAdClicked();
                }

                @Override
                public void onAdImpression(@NonNull InterstitialAd interstitialAd) {
                    adLoadCallback.reportAdImpression();
                }

                @Override
                public void onAdTTLExpired(@NonNull InterstitialAd interstitialAd) {

                }
            };
            String ad = adConfiguration.getBidResponse();
            final AdRequestParams adRequestParams = createBiddingAdRequestParams(ad);
            String adSpace = adConfiguration.getServerParameters().getString("adUnitId");
            Interstitial.loadAd(adSpace, interstitialEventListener, adRequestParams);
        }
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (!(context instanceof Activity)) {
            AdError error = new AdError(1, "Context must be an activity",
                    "com.google.ads.mediation.sample");
            adLoadCallback.onAdFailedToShow(error);
        } else {
            smaatoInterstitialAd.showAd((Activity) context);
        }
    }

}
