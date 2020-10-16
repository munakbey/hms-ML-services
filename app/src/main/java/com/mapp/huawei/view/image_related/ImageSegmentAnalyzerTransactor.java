package com.mapp.huawei.view.image_related;

import android.util.SparseArray;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;

public class ImageSegmentAnalyzerTransactor implements MLAnalyzer.MLTransactor<MLImageSegmentation> {
    @Override
    public void transactResult(MLAnalyzer.Result<MLImageSegmentation> results) {
        SparseArray<MLImageSegmentation> items = results.getAnalyseList();
        // Determine detection result processing as required. Note that only the detection results are processed.
        // Other detection-related APIs provided by ML Kit cannot be called.
    }
    @Override
    public void destroy() {
        // Callback method used to release resources when the detection ends.
    }
}