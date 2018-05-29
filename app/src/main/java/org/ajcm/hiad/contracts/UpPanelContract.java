package org.ajcm.hiad.contracts;

public interface UpPanelContract {
    interface View {
        void showTitle(String title);
    }

    interface Presenter {
        void setTitle(String title);
    }
}
