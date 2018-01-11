package com.aj.sendall.dialog;

interface AppDialog {
    void init();
    void show();
    void setOnClose(OnClose onClose);

    interface OnClose{
        void onClose();
    }
}
