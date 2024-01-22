package com.smaato.sdk.adapters.google;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.smaato.sdk.core.ad.AdRequestParams;
import com.smaato.sdk.rewarded.EventListener;
import com.smaato.sdk.rewarded.RewardedError;
import com.smaato.sdk.rewarded.RewardedInterstitial;
import com.smaato.sdk.rewarded.RewardedInterstitialAd;
import com.smaato.sdk.rewarded.RewardedRequestError;

public class SmaatoRewardedAdRenderer extends BaseAdRenderer implements MediationRewardedAd {
    private final MediationRewardedAdConfiguration mediationRewardedAdConfiguration;
    private final MediationRewardedAdCallback mediationRewardedAdCallback;
    private RewardedInterstitialAd rewardedAd;
    private EventListener rewardedEventListener;

    SmaatoRewardedAdRenderer(@NonNull MediationRewardedAdConfiguration mediationRewardedAdConfiguration, @NonNull MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> callback){
        this.mediationRewardedAdConfiguration = mediationRewardedAdConfiguration;
        mediationRewardedAdCallback = callback.onSuccess(SmaatoRewardedAdRenderer.this);
    }

    void render(){
            if(rewardedEventListener == null){
                rewardedEventListener = new EventListener() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                        rewardedAd = rewardedInterstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull RewardedRequestError rewardedRequestError) {
                        mediationRewardedAdCallback.onAdFailedToShow(new AdError(1, rewardedRequestError.getRewardedError().name(),
                                "com.smaato.sdk.adapters.google"));
                    }

                    @Override
                    public void onAdError(@NonNull RewardedInterstitialAd rewardedInterstitialAd, @NonNull RewardedError rewardedError) {
                        mediationRewardedAdCallback.onAdFailedToShow(new AdError(1,rewardedError.name(), "com.smaato.sdk.adapters.google"));
                    }

                    @Override
                    public void onAdClosed(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                        mediationRewardedAdCallback.onAdClosed();
                    }

                    @Override
                    public void onAdClicked(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                        mediationRewardedAdCallback.onAdOpened();
                        mediationRewardedAdCallback.reportAdClicked();
                    }

                    @Override
                    public void onAdStarted(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                        mediationRewardedAdCallback.reportAdImpression();
                    }

                    @Override
                    public void onAdReward(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {

                    }

                    @Override
                    public void onAdTTLExpired(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {

                    }
                };
                final String ad = mediationRewardedAdConfiguration.getBidResponse();
                final AdRequestParams adRequestParams = createBiddingAdRequestParams(ad);
                final String adSpace = mediationRewardedAdConfiguration.getServerParameters().getString("adUnitId");
                RewardedInterstitial.loadAd(adSpace, rewardedEventListener, adRequestParams);
            }
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (!(context instanceof Activity)) {
            AdError error = new AdError(1, "Context must be an activity",
                    "com.google.ads.mediation.sample");
            mediationRewardedAdCallback.onAdFailedToShow(error);
        } else {
            rewardedAd.showAd();
        }
    }
}
