package ru.hilariousstartups.analysttask1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import ru.hilariousstartups.analysttask1.model.Response;
import ru.hilariousstartups.analysttask1.service.ITaskService;
import ru.hilariousstartups.analysttask1.service.Task1Service;
import ru.hilariousstartups.analysttask1.service.Task2Service;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class AnalystTask1 implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext appContext;
    private final String inputDir;
    private final String resultLocation;
    private final Integer taskNumber;
    private final Task1Service task1Service;
    private final Task2Service task2Service;
    private final ObjectMapper objectMapper;

    public AnalystTask1(ApplicationContext appContext,
                        Task1Service task1Service,
                        Task2Service task2Service,
                        ObjectMapper objectMapper,
                        @Value("${INPUT_LOCATION:/opt/client/input}") String inputDir,
                        @Value("${RESULT_LOCATION:/opt/results/output.txt}") String resultLocation,
                        @Value("${TASK_NUMBER:1}") Integer taskNumber ) {
        this.appContext = appContext;
        this.task1Service = task1Service;
        this.task2Service = task2Service;
        this.objectMapper = objectMapper;
        this.inputDir = inputDir;
        this.resultLocation = resultLocation;
        this.taskNumber = taskNumber;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("Hello, AnalystTask1");

        Response response = null;
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream xsdIS = classloader.getResourceAsStream("xsd/task_"+ taskNumber +".xsd");
            Path xmlFilePath = null;
            for (Path path : Files.newDirectoryStream(Paths.get(inputDir))) {
                xmlFilePath = path;
                break;
            }
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdIS));
            new StreamSource(Files.newInputStream(xmlFilePath));

            ITaskService taskService = null;
            if (taskNumber == 1) {
                taskService = task1Service;
            } else if (taskNumber == 2) {
                taskService = task2Service;
            } else {
                response = new Response("Invalid TASK_NUMBER " + taskNumber);
            }

            if (taskService != null) {
                response = taskService.execute(schema, xmlFilePath);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response = new Response(e.getMessage());
        }

        try {

            String responseStr = objectMapper.writeValueAsString(response);
            log.info(responseStr);
            Files.write(Paths.get(resultLocation), responseStr.getBytes());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        SpringApplication.exit(appContext, () -> 0);
    }
}
