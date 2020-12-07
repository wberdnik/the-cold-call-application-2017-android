package com.leadinka.android.coldcall;

import android.content.Intent;

/**
 * Created by VLF on 20.05.2017.
 */

interface CC_model2guiInterface {
    String GUI_getStatusStringReady2Call();
    String GUI_getStatusStringThinking();
    String GUI_getStatusStringSMS();
    int GUI_getCurrentStatus();

    boolean GUI_startAuthorization();


    void GUI_FixDraft(Intent data);
    void GUI_UnavailableNumber();
    void GUI_RefuseClient();
    void GUI_LFSkipSMS();



    void GUI_OpenActivityNewLead(ColdCallActivity activity, int CodeActivity);
    void GUI_OpenActivityReport(ColdCallActivity activity, int CodeActivity);

    String GUI_getPhone2CallAndStartCalling();
    void GUI_StopCalling();

    int GUI_ThinkingOrReady();

}
