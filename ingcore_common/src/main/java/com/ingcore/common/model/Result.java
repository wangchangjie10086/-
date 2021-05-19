package com.ingcore.common.model;

import com.ingcore.common.contants.ResultContants;

public class Result<T> {

    private Integer resultCode = ResultContants.RESULT_CODE_SUCCESS;

    private String resultDesc = ResultContants.RESULT_DESC_SUCCESS;

    private T resultData;

    public Result() {
    }

    public Result(Integer resultCode, String resultDesc) {
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
    }

    public Result(Integer resultCode, String resultDesc, T resultData) {
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
        this.resultData = resultData;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    public T getResultData() {
        return resultData;
    }

    public void setResultData(T resultData) {
        this.resultData = resultData;
    }
}
