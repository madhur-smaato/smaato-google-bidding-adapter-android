package com.smaato.sdk.adapters.google;

import android.util.Log;

import com.smaato.sdk.core.ad.AdRequestParams;
import com.smaato.sdk.iahb.InAppBid;
import com.smaato.sdk.iahb.InAppBiddingException;
import com.smaato.sdk.iahb.SmaatoSdkInAppBidding;

public class BaseAdRenderer {

    AdRequestParams createBiddingAdRequestParams(final String bidResponse) {
        final String token;
        try {
            final InAppBid inAppBid = InAppBid.create(bidResponse);
            token = SmaatoSdkInAppBidding.saveBid(inAppBid);
        } catch (final InAppBiddingException exception) {
            Log.i("Error saving pre-bid:" + bidResponse, exception.getMessage());
            return null;
        }
        return AdRequestParams.builder().setUBUniqueId(token).build();
    }
}
