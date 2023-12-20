package com.smaato.sdk.adapters.google;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.VersionInfo;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;
import com.google.android.gms.ads.mediation.rtb.RtbAdapter;
import com.google.android.gms.ads.mediation.rtb.RtbSignalData;
import com.google.android.gms.ads.mediation.rtb.SignalCallbacks;
import com.smaato.sdk.core.BuildConfig;
import com.smaato.sdk.core.Config;
import com.smaato.sdk.core.SmaatoSdk;
import com.smaato.sdk.core.log.LogLevel;

import java.util.List;
import java.util.regex.Pattern;

public class SmaatoGoogleMediationAdapter extends RtbAdapter {
    private final String TAG = "SmaatoMediationAdapter";
    private Context context;
    @Override
    public void collectSignals(@NonNull RtbSignalData rtbSignalData, @NonNull SignalCallbacks signalCallbacks) {
        String signals = SmaatoSdk.collectSignals(context);
        signalCallbacks.onSuccess(signals);
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        String versionString = SmaatoSdk.getVersion();
        String splits[] = versionString.split("\\.");
        if (splits.length >= 3) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]);
            return new VersionInfo(major, minor, micro);
        }

        String logMessage = String.format("Unexpected SDK version format: %s." +
                "Returning 0.0.0 for SDK version.", versionString);
        Log.w(TAG, logMessage);
        return new VersionInfo(0, 0, 0);
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        String versionString = BuildConfig.VERSION_NAME;
        String splits[] = versionString.split("\\.");
        if (splits.length >= 4) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]) * 100 + Integer.parseInt(splits[3]);
            return new VersionInfo(major, minor, micro);
        }

        String logMessage = String.format("Unexpected adapter version format: %s." +
                "Returning 0.0.0 for adapter version.", versionString);
        Log.w(TAG, logMessage);
        return new VersionInfo(0, 0, 0);
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> list) {
        initialiseSdk(list, context);
        try {
            Thread.sleep(500);
            initializationCompleteCallback.onInitializationSucceeded();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration, @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        SmaatoBannerAdRenderer bannerRenderer =
                new SmaatoBannerAdRenderer(mediationBannerAdConfiguration, callback);
        bannerRenderer.render();
    }

    @Override
    public void loadInterstitialAd(@NonNull MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration, @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback) {
        SmaatoInterstitialAdRenderer interstitialAdRenderer = new SmaatoInterstitialAdRenderer(mediationInterstitialAdConfiguration, callback);
        interstitialAdRenderer.render();
    }

    private void initialiseSdk(List<MediationConfiguration> list, Context context) {
        this.context = context;
        if (context != null && list != null && !list.isEmpty()) {
            MediationConfiguration mediationConfiguration = list.get(0);
            final String publisherAppId = mediationConfiguration.getServerParameters() != null ?
                    mediationConfiguration.getServerParameters().getString("app_id", null) : null;
            final String pubIdCustomParam = mediationConfiguration.getServerParameters() != null ?
                    mediationConfiguration.getServerParameters().getString("sma_pub_id", null) : null;
            Log.i("SmaatoSDK id found as:", publisherAppId);
            Log.i("SmaatoSDK custom pub id", pubIdCustomParam + "...");
            final Config config = Config.builder()
                    .setLogLevel(LogLevel.DEBUG)
                    .enableLogging(true)
                    .setHttpsOnly(true)
                    .build();

            final Application application = (Application) context;
            if (isValidPublisherId(pubIdCustomParam)) {
                Log.i("SmaatoSDK init with ", pubIdCustomParam);
                SmaatoSdk.init(application, config, pubIdCustomParam);
            } else if (isValidPublisherId(publisherAppId)) {
                Log.i("SmaatoSDK init with: ", publisherAppId);
                SmaatoSdk.init(application, config, publisherAppId);
            }

        } else {
            System.out.print("SmaatoSDK cannot find app context, failed to initialise SDK");
        }
    }

    private boolean isNotInitialised() {
        return !SmaatoSdk.isSmaatoSdkInitialised() || (SmaatoSdk.getPublisherId() == null
                || (SmaatoSdk.getPublisherId() != null && SmaatoSdk.getPublisherId().isEmpty()));
    }

    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    private boolean isValidPublisherId(String cmgId) {
        return cmgId != null && !cmgId.isEmpty() && isNumeric(cmgId);
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
