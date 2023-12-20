package com.smaato.sdk.adapters.google;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.smaato.sdk.banner.ad.BannerAdSize;
import com.smaato.sdk.banner.widget.BannerError;
import com.smaato.sdk.banner.widget.BannerView;

public class SmaatoBannerAdRenderer extends BaseAdRenderer implements MediationBannerAd {
    private final MediationBannerAdConfiguration adConfiguration;
    private final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adLoadCallback;
    private BannerView adView;
    private MediationBannerAdCallback callback;
    private BannerView.EventListener bannerEventListener;

    public SmaatoBannerAdRenderer(
            MediationBannerAdConfiguration adConfiguration,
            MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adLoadCallback) {
        this.adConfiguration = adConfiguration;
        this.adLoadCallback = adLoadCallback;
    }

    @NonNull
    @Override
    public View getView() {
        return adView;
    }

    void render() {
        adView = new BannerView(adConfiguration.getContext());
        BannerAdSize bannerAdSize = transformAdSize(adConfiguration.getAdSize());
        if(bannerEventListener == null){
            bannerEventListener = new BannerView.EventListener() {
                @Override
                public void onAdLoaded(@NonNull BannerView bannerView) {
                    callback = adLoadCallback.onSuccess(SmaatoBannerAdRenderer.this);
                }

                @Override
                public void onAdFailedToLoad(@NonNull BannerView bannerView, @NonNull BannerError bannerError) {
                    adLoadCallback.onFailure("Error: " + bannerError.toString());
                }

                @Override
                public void onAdImpression(@NonNull BannerView bannerView) {
                    if (callback != null) {
                        callback.reportAdImpression();
                    }
                }

                @Override
                public void onAdClicked(@NonNull BannerView bannerView) {
                    if (callback != null) {
                        callback.onAdOpened();
                        callback.reportAdClicked();
                    }
                }

            };
        }
        adView.setEventListener(bannerEventListener);
        String ad = adConfiguration.getBidResponse();
        String adSpace = adConfiguration.getServerParameters().getString("adUnitId");
        adView.loadAd(adSpace,bannerAdSize, createBiddingAdRequestParams(ad));
    }



    private BannerAdSize transformAdSize(AdSize adSize) {
        switch (adSize.getHeight()) {
            case 50:
                return BannerAdSize.XX_LARGE_320x50;
            case 250:
                return BannerAdSize.MEDIUM_RECTANGLE_300x250;
            case 90:
                return BannerAdSize.LEADERBOARD_728x90;
            default:
                return BannerAdSize.MEDIUM_RECTANGLE_300x250;
        }
    }
}
