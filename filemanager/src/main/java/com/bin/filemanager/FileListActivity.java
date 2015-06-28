package com.bin.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by jabin on 6/25/15.
 */
public class FileListActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    final static String PREF_DEFAULT_URI = "pref_default_uri";
    static String sdcardPath;
    static String extSdcardPath;
    ListView listView;
    FileListAdapter adapter;
    List<FileItem> list = new ArrayList<>();
    Uri curUri;
    DocumentFile curFile;
    Stack<Uri> stack = new Stack<>();
    List<FileItem> selectedFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        listView = (ListView) findViewById(R.id.listview);
        adapter = new FileListAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        Bundle bundle = getIntent().getExtras();
        sdcardPath = bundle.getString("sdcardPath");
        extSdcardPath = bundle.getString("extSdcardPath");


        if (!TextUtils.isEmpty(extSdcardPath)) {
            String strUri = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_DEFAULT_URI, null);
            if (TextUtils.isEmpty(strUri)) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 101);
            } else {
                Uri uri = Uri.parse(strUri);
                DocumentFile rootDocumentfile = DocumentFile.fromTreeUri(this, uri);
                updateViews1(rootDocumentfile);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            /*Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));

            Cursor docCursor = contentResolver.query(docUri, new String[]{
                    Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                while (docCursor.moveToNext()) {
                    Log.d(TAG, "found doc =" + docCursor.getString(0) + ", mime=" + docCursor
                            .getString(1));
                    mCurrentDirectoryUri = uri;
                    mCurrentDirectoryTextView.setText(docCursor.getString(0));
                    mCreateDirectoryButton.setEnabled(true);
                }
            } finally {
                closeQuietly(docCursor);
            }*/

//            Log.d("Uri", "data uri: " + uri);
//            Uri childUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
//
//            curUri = childUri;
//            updateViews(curUri);
//            stack.push(childUri);
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_DEFAULT_URI, uri.toString()).commit();
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
            updateViews1(documentFile);
        }
    }

    private void updateViews1(DocumentFile documentFile) {
        if (documentFile.isDirectory()) {
            list.clear();
            curFile = documentFile;
            DocumentFile[] documentFiles = documentFile.listFiles();
            for (DocumentFile file : documentFiles) {
                FileItem item = new FileItem();
                item.file = file;
                item.fileName = file.getName();
                item.lastModified = file.lastModified();
                item.type = file.getType();
                item.parentFile = file.getParentFile();
                item.uri = file.getUri();
                item.size = file.length();
                list.add(item);
            }
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateViews(Uri uri) {
        try {
            Cursor childCursor = getContentResolver().query(uri, null, null, null, null);
            list.clear();
            while (childCursor.moveToNext()) {
                FileItem item = new FileItem();
                item.fileName = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                item.lastModified = childCursor.getLong(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                item.type = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                item.size = childCursor.getLong(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                item.uri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, childCursor.getString(0));
                Log.d("Uri", "file Uri: " + item.uri + "");
                list.add(item);
            }
            childCursor.close();
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = (FileItem) parent.getAdapter().getItem(position);
//        DocumentsContract.renameDocument(getContentResolver(),fileItem.uri,"aa1");
//        DocumentsContract.deleteDocument(getContentResolver(),fileItem.uri);
//        updateViews(curUri);
        if (fileItem.file.isDirectory()) {
            if (selectedFile.size() == 0) {
                updateViews1(fileItem.file);
            } else {
                Object[] seletedFileArray =  selectedFile.toArray();
                for (Object item : seletedFileArray) {
                    FileHelper.copyFile(this, (FileItem)item, fileItem);
                    Toast.makeText(this, ((FileItem)item).fileName + " copied to " + fileItem.fileName, Toast.LENGTH_SHORT).show();
                }
                selectedFile.clear();
            }
        } else {
            shareFile(fileItem);
        }

    }

    private void shareFile(FileItem fileItem) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, fileItem.uri);
        intent.setType("text/plain");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        if (stack.size() > 1) {
//            Uri uri = stack.remove(stack.size() - 2);
//            updateViews(uri);
//        } else {
//            stack.clear();
//            super.onBackPressed();
//        }
        DocumentFile parentfile = curFile.getParentFile();
        if (parentfile != null) {
            updateViews1(parentfile);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = (FileItem) parent.getAdapter().getItem(position);
        selectedFile.add(fileItem);
        Toast.makeText(this, fileItem.fileName + " added to copy", Toast.LENGTH_SHORT).show();
        return false;
    }
}
