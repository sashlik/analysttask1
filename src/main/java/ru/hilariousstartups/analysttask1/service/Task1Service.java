package ru.hilariousstartups.analysttask1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import ru.hilariousstartups.analysttask1.model.Response;
import ru.hilariousstartups.analysttask1.model.StatusType;
import ru.hilariousstartups.analysttask1.task1.gen.TraceList;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Task1Service implements  ITaskService {


    private final Map<Integer, Map<String, List<Integer>>> reference;

    public Task1Service(@Qualifier("task1refMap") Map<Integer, Map<String, List<Integer>>> reference) {
        this.reference = reference;
    }

    @Override
    public Response execute(Schema schema, Path xmlFilePath) {
        Response response = new Response();
        try {
            StreamSource xmlSource = new StreamSource(Files.newInputStream(xmlFilePath));
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
            response.setValidationQuality(1);

            JAXBContext jc = JAXBContext.newInstance(TraceList.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            xmlSource = new StreamSource(Files.newInputStream(xmlFilePath));
            TraceList traceList = (TraceList) unmarshaller.unmarshal(xmlSource.getInputStream());

            Integer score = score(traceList);

            response.setTestingQuality(score);
        } catch (SAXException e) {
            response.setStatusType(StatusType.OK);
            response.setValidationQuality(0);
            response.setTestingQuality(0);
        } catch (JAXBException | IOException e) {
            response = new Response(e.getMessage());
        }


        return response;
    }

    private Integer score(TraceList traceList) {
        Map<Integer, Map<String, List<Integer>>> traceMap = toMap(traceList);
        Integer score = 0;
        for (Map.Entry<Integer, Map<String, List<Integer>>> mapEntry : traceMap.entrySet()) {
            Integer scorePerTrace = 0;
            Integer traceNo = mapEntry.getKey();
            Map<String, List<Integer>> referenceTrace = reference.get(traceNo);
            if (referenceTrace == null) {
                scorePerTrace = -2;
            }
            else {
                List<Integer> referenceTraceFlat = flatReleaseMap(referenceTrace);
                for (Map.Entry<String, List<Integer>> curTrace : mapEntry.getValue().entrySet()) {
                    String curRelease = curTrace.getKey();
                    List<Integer> curReleaseReqs = curTrace.getValue();
                    for (Integer curReleaseReq : curReleaseReqs) {
                        if (curReleaseReq == 6) {
                            continue;
                        }
                        else if (curReleaseReq == 17 || curReleaseReq == 24) {
                            scorePerTrace -= 4;
                            continue;
                        }
                        if (referenceTraceFlat.contains(curReleaseReq)) {
                            scorePerTrace += 2;
                            if (curRelease != null) {
                                if (referenceTrace.get(curRelease) != null && referenceTrace.get(curRelease).contains(curReleaseReq)) {
                                    scorePerTrace += 1;
                                } else {
                                    scorePerTrace -= 2;
                                }
                            }
                        } else {
                            scorePerTrace -= 2;
                        }
                    }
                }
                if (referenceTraceFlat.equals(flatReleaseMap(mapEntry.getValue()))) {
                    scorePerTrace += 1;
                }
            }
            log.info("TRACE " + traceNo + ": " + scorePerTrace);
            score += scorePerTrace;
        }
        return score;
    }

    private Map<Integer, Map<String, List<Integer>>> toMap(TraceList traceList) {
        Map<Integer, Map<String, List<Integer>>> map = new HashMap<>();
        for (TraceList.Trace trace : traceList.getTrace()) {
            Map<String, List<Integer>> releaseMap = new HashMap<>();
            for (TraceList.Trace.Requirement requirement : trace.getRequirement()) {
                List<Integer> reqs = releaseMap.computeIfAbsent(requirement.getRelease(), k -> new ArrayList<>());
                reqs.add(requirement.getValue());
            }

            map.put(trace.getInputNumber(), releaseMap);
        }
        return map;
    }

    private List<Integer> flatReleaseMap(Map<String, List<Integer>> releaseMap) {
        return releaseMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
