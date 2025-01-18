package uou.alarm_it.apiPayload.exception.handler;

import uou.alarm_it.apiPayload.code.BaseErrorCode;
import uou.alarm_it.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
