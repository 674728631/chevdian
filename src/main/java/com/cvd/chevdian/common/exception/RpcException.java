package com.cvd.chevdian.common.exception;

import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcException extends BusinessException {

    /**
     * Instantiates a new Uac rpc exception.
     */
    public RpcException() {
    }

    /**
     * Instantiates a new Uac rpc exception.
     *
     * @param code      the code
     * @param msgFormat the msg format
     * @param args      the args
     */
    public RpcException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
        log.info("<== MdcRpcException, code:{}, message:{}", this.code, super.getMessage());
    }

    /**
     * Instantiates a new Uac rpc exception.
     *
     * @param code the code
     * @param msg  the msg
     */
    public RpcException(int code, String msg) {
        super(code, msg);
        log.info("<== MdcRpcException, code:{}, message:{}", this.code, super.getMessage());
    }

    /**
     * Instantiates a new Mdc rpc exception.
     *
     * @param codeEnum the code enum
     */
    public RpcException(ErrorCodeEnum codeEnum) {
        super(codeEnum.code(), codeEnum.msg());
        log.info("<== MdcRpcException, code:{}, message:{}", this.code, super.getMessage());
    }

    /**
     * Instantiates a new Mdc rpc exception.
     *
     * @param codeEnum the code enum
     * @param args     the args
     */
    public RpcException(ErrorCodeEnum codeEnum, Object... args) {
        super(codeEnum, args);
        log.info("<== MdcRpcException, code:{}, message:{}", this.code, super.getMessage());
    }
}
