package com.fc.v2.common.exception;

/**
 * 跨区作业证核准流转违例：抢步、越岗、替签、压死件再走、重复出证等
 * 一律由业务服务层抛出，页面只认服务层结论。
 *
 * @author fuce
 * @date 2026-09-27
 */
public class AmssFlowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AmssFlowException(String message) {
        super(message);
    }
}
