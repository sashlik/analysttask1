package ru.hilariousstartups.analysttask1.service;

import ru.hilariousstartups.analysttask1.model.Response;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.nio.file.Path;

public interface ITaskService {

    Response execute(Schema schema, Path xmlSource);

}
