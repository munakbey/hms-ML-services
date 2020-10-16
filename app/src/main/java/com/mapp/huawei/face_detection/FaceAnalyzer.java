package com.mapp.huawei.face_detection;

import android.util.SparseArray;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.face.MLFace;
import com.mapp.huawei.object_detection.GraphicOverlay;

public class FaceAnalyzer implements MLAnalyzer.MLTransactor<MLFace> {
    private GraphicOverlay mGraphicOverlay;

    public FaceAnalyzer(GraphicOverlay ocrGraphicOverlay) {
        this.mGraphicOverlay = ocrGraphicOverlay;
    }

    @Override
    public void transactResult(MLAnalyzer.Result<MLFace> result) {
        this.mGraphicOverlay.clear();
        SparseArray<MLFace> faceSparseArray = result.getAnalyseList();
        for (int i = 0; i < faceSparseArray.size(); i++) {
            // todo step 4: add on-device face graphic
            MLFaceGraphic graphic = new MLFaceGraphic(this.mGraphicOverlay, faceSparseArray.valueAt(i));
            this.mGraphicOverlay.add(graphic);
            // finish
        }
    }

    @Override
    public void destroy() {
        this.mGraphicOverlay.clear();
    }

}