package top.linrty.netdisk.common.domain.dto;


import top.linrty.netdisk.common.enums.ResultCodeEnum;

import java.util.List;


public class RestResult<T> {

    private Boolean success = true;

    private Integer code = 0;

    private String message;

    private T data;

    private List<T> dataList;

    private long total;

    public static RestResult success() {
        RestResult r = new RestResult();
        r.setSuccess(ResultCodeEnum.SUCCESS.getSuccess());
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        return r;
    }

    public static RestResult fail() {
        RestResult r = new RestResult();
        r.setSuccess(ResultCodeEnum.UNKNOWN_ERROR.getSuccess());
        r.setCode(ResultCodeEnum.UNKNOWN_ERROR.getCode());
        r.setMessage(ResultCodeEnum.UNKNOWN_ERROR.getMessage());
        return r;
    }

    public static RestResult setResult(ResultCodeEnum result) {
        RestResult r = new RestResult();
        r.setSuccess(result.getSuccess());
        r.setCode(result.getCode());
        r.setMessage(result.getMessage());
        return r;
    }

    public RestResult data(T param) {
        this.setData(param);
        return this;
    }

    public RestResult dataList(List<T> param, long total) {
        this.setDataList(param);
        this.setTotal(total);
        return this;
    }

    public RestResult message(String message) {
        this.setMessage(message);
        return this;
    }

    public RestResult code(Integer code) {
        this.setCode(code);
        return this;
    }

    public RestResult success(Boolean success) {
        this.setSuccess(success);
        return this;
    }

    public RestResult() {
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public List<T> getDataList() {
        return this.dataList;
    }

    public long getTotal() {
        return this.total;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public void setCode(final Integer code) {
        this.code = code;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public void setDataList(final List<T> dataList) {
        this.dataList = dataList;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RestResult)) {
            return false;
        } else {
            RestResult<?> other = (RestResult)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getTotal() != other.getTotal()) {
                return false;
            } else {
                label73: {
                    Object this$success = this.getSuccess();
                    Object other$success = other.getSuccess();
                    if (this$success == null) {
                        if (other$success == null) {
                            break label73;
                        }
                    } else if (this$success.equals(other$success)) {
                        break label73;
                    }

                    return false;
                }

                Object this$code = this.getCode();
                Object other$code = other.getCode();
                if (this$code == null) {
                    if (other$code != null) {
                        return false;
                    }
                } else if (!this$code.equals(other$code)) {
                    return false;
                }

                label59: {
                    Object this$message = this.getMessage();
                    Object other$message = other.getMessage();
                    if (this$message == null) {
                        if (other$message == null) {
                            break label59;
                        }
                    } else if (this$message.equals(other$message)) {
                        break label59;
                    }

                    return false;
                }

                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
                    return false;
                }

                Object this$dataList = this.getDataList();
                Object other$dataList = other.getDataList();
                if (this$dataList == null) {
                    if (other$dataList != null) {
                        return false;
                    }
                } else if (!this$dataList.equals(other$dataList)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RestResult;
    }

    public int hashCode() {
        int result = 1;
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        Object $success = this.getSuccess();
        result = result * 59 + ($success == null ? 43 : $success.hashCode());
        Object $code = this.getCode();
        result = result * 59 + ($code == null ? 43 : $code.hashCode());
        Object $message = this.getMessage();
        result = result * 59 + ($message == null ? 43 : $message.hashCode());
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        Object $dataList = this.getDataList();
        result = result * 59 + ($dataList == null ? 43 : $dataList.hashCode());
        return result;
    }

    public String toString() {
        return "RestResult(success=" + this.getSuccess() + ", code=" + this.getCode() + ", message=" + this.getMessage() + ", data=" + this.getData() + ", dataList=" + this.getDataList() + ", total=" + this.getTotal() + ")";
    }
}
