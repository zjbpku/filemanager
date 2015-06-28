package com.bin.filemanager;

import android.net.Uri;
import android.support.v4.provider.DocumentFile;

/**
 * Created by jabin on 6/25/15.
 */
public class FileItem {
    public DocumentFile file;
    public DocumentFile parentFile;
    public Uri uri;
    public String fileName;
    public String filePath;
    public String type;
    public long lastModified;
    public long size;
}
