package com.dnake;

import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {
    protected View inflaterView;
    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void doCancel();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mCallback != null)
            mCallback.doCancel();
    }
}
