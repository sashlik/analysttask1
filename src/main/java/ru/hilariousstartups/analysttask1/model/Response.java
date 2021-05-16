package ru.hilariousstartups.analysttask1.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response {

    public Response(String errors) {
        this.statusType = StatusType.ERR;
        this.errors = errors;
    }

    private StatusType statusType;
    private Integer validationQuality;
    private Integer testingQuality;
    private String errors;

}
