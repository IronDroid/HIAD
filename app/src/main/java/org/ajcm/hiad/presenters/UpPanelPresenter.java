package org.ajcm.hiad.presenters;

import android.util.Log;

import org.ajcm.hiad.contracts.UpPanelContract;
import org.ajcm.hiad.models.UpPanel;

public class UpPanelPresenter implements UpPanelContract.Presenter {

    private static final String TAG = "UpPanelPresenter";
    private UpPanelContract.View view;
    private UpPanel upPanel;

    public UpPanelPresenter(UpPanelContract.View view) {
        this.view = view;
        upPanel = new UpPanel();
    }

    @Override
    public void setTitle(String title) {
        upPanel.setTitle(title);
        view.showTitle(upPanel.getTitle());
        Log.e(TAG, "setTitle: " + title);
    }
}
