package com.hfstudio.functionalstorage.client.model;

import com.gtnewhorizon.gtnhlib.client.model.BakeData;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;

public class FaceRotatedModel extends JSONModel {

    public FaceRotatedModel(JSONModel parent) {
        super(parent);
    }

    @Override
    public synchronized BakedModel bake(BakeData data) {
        return super.bake(data);
    }
}
