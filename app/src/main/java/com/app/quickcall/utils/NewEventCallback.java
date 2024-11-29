package com.app.quickcall.utils;

import com.app.quickcall.model.CallModel;

public interface NewEventCallback {
    void onNewEventReceived(CallModel model);
}
