/*
 *    Copyright 2018 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.appnexus.opensdk.viewability;

import android.webkit.WebView;

import com.appnexus.opensdk.utils.Clog;
import com.appnexus.opensdk.utils.StringUtil;
import com.iab.omid.library.appnexus.ScriptInjector;
import com.iab.omid.library.appnexus.adsession.AdEvents;
import com.iab.omid.library.appnexus.adsession.AdSession;
import com.iab.omid.library.appnexus.adsession.AdSessionConfiguration;
import com.iab.omid.library.appnexus.adsession.AdSessionContext;
import com.iab.omid.library.appnexus.adsession.Owner;

public class ANOmidBannerHTMLAdSession {

    private AdSession omidAdSession;

    public String prependOMIDJSToHTML(String html) {
        try {
            String htmlString = html;
            if(!StringUtil.isEmpty(ANOmidViewabilty.getInstance().getOmidJsServiceContent())) {
                htmlString = ScriptInjector.injectScriptContentIntoHtml(ANOmidViewabilty.getInstance().getOmidJsServiceContent(),
                        html);
            }
            return htmlString;
        } catch (Exception e) {
            e.printStackTrace();
            // Return original HTML if there was an error
            return html;
        }
    }

    public  void initAdSession(WebView webView) {
        try {
            String customReferenceData = "";
            AdSessionContext adSessionContext = AdSessionContext.createHtmlAdSessionContext(ANOmidViewabilty.getInstance().getAppnexusPartner(), webView,
                    customReferenceData);

            Owner owner = Owner.NATIVE;

            AdSessionConfiguration adSessionConfiguration =
                    AdSessionConfiguration.createAdSessionConfiguration(owner, null, false);


            omidAdSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);
            omidAdSession.registerAdView(webView);
            omidAdSession.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException exception){
            Clog.e(Clog.baseLogTag, "OMID Ad Session - Exception", exception);
        }
    }

    public void fireImpression(){
        if(omidAdSession !=null) {
            try {
                AdEvents adEvents = AdEvents.createAdEvents(omidAdSession);
                adEvents.impressionOccurred();
            } catch (IllegalArgumentException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public  void stopAdSession() {
        if (omidAdSession != null) {
            omidAdSession.finish();
            omidAdSession = null;
        }
    }


}
