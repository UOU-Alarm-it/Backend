package uou.alarm_it.apiPayload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uou.alarm_it.apiPayload.code.BaseErrorCode;
import uou.alarm_it.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() {
        return this.code.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}
