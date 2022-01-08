package de.szalkowski.activitylauncher;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class AsyncProvider<ReturnType> extends AsyncTask<Void, Integer, ReturnType> {
    private final CharSequence message;
    private final Listener<ReturnType> listener;
    private int max;
    private MaterialAlertDialogBuilder dialog;
    private final ProgressBar progress;
    AlertDialog progressDialog;

    AsyncProvider(Context context, Listener<ReturnType> listener, boolean showProgressDialog) {
        this.message = context.getText(R.string.dialog_progress_loading);
        this.listener = listener;

        if (showProgressDialog) {
            this.dialog = new MaterialAlertDialogBuilder(context);
            this.dialog.setTitle(this.message)
                    .setMessage(R.string.title_dialog_disclaimer);

            progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progress.setLayoutParams(lp);
            dialog.setView(progress);

        } else {
            progress = null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (this.progress != null && values.length > 0) {
            int value = values[0];

            if (value == 0) {
                this.progress.setIndeterminate(false);
                this.progress.setMax(this.max);
            }
            this.progressDialog.setMessage(String.valueOf(value) + "/" + String.valueOf(max));
            this.progress.setProgress(value);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (this.progress != null) {

            this.dialog.setCancelable(false);
            //this.progress.setMessage(this.message);
            this.progress.setIndeterminate(true);
            //this.progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            progressDialog = dialog.create();
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(ReturnType result) {
        super.onPostExecute(result);
        if (this.listener != null) {
            this.listener.onProviderFinished(this, result);
        }

        if (this.progress != null) {
            try {
                this.progressDialog.dismiss();
            } catch (IllegalArgumentException e) { /* ignore */ }
        }
    }

    abstract protected ReturnType run(Updater updater);

    @Override
    protected ReturnType doInBackground(Void... params) {
        return run(new Updater(this));
    }

    public interface Listener<ReturnType> {
        void onProviderFinished(AsyncProvider<ReturnType> task, ReturnType value);
    }

    class Updater {
        private final AsyncProvider<ReturnType> provider;

        Updater(AsyncProvider<ReturnType> provider) {
            this.provider = provider;
        }

        void update(int value) {
            this.provider.publishProgress(value);
        }

        void updateMax(int value) {
            this.provider.max = value;
        }
    }
}
