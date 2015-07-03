package cx.hell.android.pdfviewpro;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import cn.me.archko.pdf.AKProgress;
import cn.me.archko.pdf.AKRecent;
import cn.me.archko.pdf.LengthUtils;
import cn.me.archko.pdf.Util;
import com.artifex.mupdfdemo.R;

import java.io.File;
import java.util.ArrayList;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 11-11-17
 */
public class HistoryFragment extends BrowserFragment {

    public static final String TAG="HistoryFragment";
    private Boolean showExtension=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        this.optionsMenuItem=menu.add(R.string.options);
        MenuItemCompat.setShowAsAction(this.optionsMenuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        backMenuItem=menu.add(R.string.options_back);
        MenuItemCompat.setShowAsAction(this.backMenuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        restoreMenuItem=menu.add(R.string.options_restore);
        MenuItemCompat.setShowAsAction(this.restoreMenuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem==this.backMenuItem) {
            backup();
        } else if (menuItem==this.restoreMenuItem) {
            restore();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void backup() {
        final ProgressDialog progressDialog=new ProgressDialog(getActivity());
        final long now=System.currentTimeMillis();
        Util.execute(true, new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String filepath=AKRecent.getInstance(APVApplication.getInstance()).backupFromDb();
                long newTime=System.currentTimeMillis()-now;
                if (newTime<1500l) {
                    newTime=1500l-newTime;
                } else {
                    newTime=0;
                }

                try {
                    Thread.sleep(newTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return filepath;
            }

            @Override
            protected void onPostExecute(String s) {
                if (null!=progressDialog) {
                    progressDialog.dismiss();
                }

                if (!LengthUtils.isEmpty(s)) {
                    Log.d("", "file:"+s);
                    Toast.makeText(APVApplication.getInstance(), "备份成功:"+s, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(APVApplication.getInstance(), "备份失败", Toast.LENGTH_LONG).show();
                }
            }
        }, (Void[]) null);
    }

    private void restore() {
        final ProgressDialog progressDialog=new ProgressDialog(getActivity());
        final long now=System.currentTimeMillis();
        Util.execute(true, new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                String filepath=null;
                String[] filenames=Environment.getExternalStorageDirectory().list();
                if (null==filenames) {
                    return false;
                }

                for (String s : filenames) {
                    if (s.startsWith("mupdf_")) {
                        filepath=Environment.getExternalStorageDirectory()+File.separator+s;
                        Log.d(TAG, "restore file:"+s);
                    }
                }

                if (null==filepath) {
                    return false;
                }
                boolean flag=AKRecent.getInstance(APVApplication.getInstance()).restoreToDb(filepath);
                long newTime=System.currentTimeMillis()-now;
                if (newTime<1500l) {
                    newTime=1500l-newTime;
                } else {
                    newTime=0;
                }

                try {
                    Thread.sleep(newTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return flag;
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if (null!=progressDialog) {
                    progressDialog.dismiss();
                }

                if (s) {
                    Toast.makeText(APVApplication.getInstance(), "恢复成功:"+s, Toast.LENGTH_LONG).show();
                    update();
                } else {
                    Toast.makeText(APVApplication.getInstance(), "恢复失败", Toast.LENGTH_LONG).show();
                }
            }
        }, (Void[]) null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);
        filesListView.setDivider(null);
        filesListView.setDividerHeight(0);

        this.pathTextView.setVisibility(View.GONE);

        return view;
    }

    public void update() {
        if (null==fileListAdapter) {
            return;
        }
        this.fileListAdapter.setMode(AKAdapter.TYPE_RENCENT);

        Util.execute(true, new AsyncTask<Void, Void, ArrayList<FileListEntry>>() {
            @Override
            protected ArrayList<FileListEntry> doInBackground(Void... params) {
                AKRecent recent=AKRecent.getInstance(HistoryFragment.this.getActivity());
                ArrayList<AKProgress> progresses=recent.readRecentFromDb();
                //Log.d(TAG, "progresses:"+progresses);
                ArrayList<FileListEntry> entryList=new ArrayList<FileListEntry>();
                if (null!=progresses&&progresses.size()>0) {
                    FileListEntry entry;
                    for (AKProgress progress : progresses) {
                        try {
                            entry=new FileListEntry(FileListEntry.RECENT, 0, new File(progress.path), showExtension);
                            entry.setAkProgress(progress);
                            entryList.add(entry);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    /*if (entryList.size()>0) {
                        try {
                            Collections.sort(entryList, new Comparator<FileListEntry>() {
                                public int compare(FileListEntry f1, FileListEntry f2) {
                                    if (f1==null) throw new RuntimeException("f1 is null inside sort");
                                    if (f2==null) throw new RuntimeException("f2 is null inside sort");
                                    try {
                                        return f1.getAkProgress().compare(f1.getAkProgress(), f2.getAkProgress());
                                    } catch (NullPointerException e) {
                                        throw new RuntimeException("failed to compare "+f1+" and "+f2, e);
                                    }
                                }
                            });
                        } catch (NullPointerException e) {
                            throw new RuntimeException("failed to sort file list "+" for path ", e);
                        }
                    }*/
                }

                return entryList;
            }

            @Override
            protected void onPostExecute(ArrayList<FileListEntry> entries) {
                if (null!=entries&&entries.size()>0) {
                    fileList=entries;
                    fileListAdapter.setData(fileList);
                    fileListAdapter.notifyDataSetChanged();
                    //filesListView.setSelection(0);
                    mSwipeRefreshWidget.setRefreshing(false);
                }
            }
        }, (Void[]) null);
    }
}
