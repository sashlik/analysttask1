package ru.hilariousstartups.analysttask1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import ru.hilariousstartups.analysttask1.model.ProviderConsumer;
import ru.hilariousstartups.analysttask1.model.Response;
import ru.hilariousstartups.analysttask1.model.StatusType;
import ru.hilariousstartups.analysttask1.task1.gen.TraceList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Task2Service implements  ITaskService {

    @Override
    public Response execute(Schema schema, Path xmlFilePath) {
        Response response = new Response();
        try {
            StreamSource xmlSource = new StreamSource(Files.newInputStream(xmlFilePath));
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
            response.setValidationQuality(1);

            JAXBContext jc = JAXBContext.newInstance(ru.hilariousstartups.analysttask1.task2.gen.Service.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            xmlSource = new StreamSource(Files.newInputStream(xmlFilePath));
            ru.hilariousstartups.analysttask1.task2.gen.Service service = (ru.hilariousstartups.analysttask1.task2.gen.Service) unmarshaller.unmarshal(xmlSource.getInputStream());

            Integer score = score(service);
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

    private Integer score(ru.hilariousstartups.analysttask1.task2.gen.Service service) {
        if (service.getIntegration() == null || service.getIntegration().isEmpty()) {
            return 0;
        }
        Integer score = 0;
        List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> alphaClient = filter(service,"Alpha", "Client");

        if (alphaClient.size() == 2) {
            if ("http".equals(alphaClient.get(0).getType()) && "http".equals(alphaClient.get(1).getType())) {
                if (alphaClient.get(0).getDataFromat().equals(alphaClient.get(1).getDataFromat())) {
                    if ("json".equalsIgnoreCase(alphaClient.get(0).getDataFromat())) {
                        score += 10;
                    } else if ("xml".equalsIgnoreCase(alphaClient.get(0).getDataFromat())) {
                        score += 6;
                    }
                    if (!alphaClient.get(0).getDataType().contains("metaData")) {
                        score -= 1;
                    }
                }
            }
        }
        else if (alphaClient.size() == 1) {
            if ("http".equals(alphaClient.get(0).getType())) {
                if ("json".equalsIgnoreCase(alphaClient.get(0).getDataFromat())) {
                    score += 10;
                } else if ("xml".equalsIgnoreCase(alphaClient.get(0).getDataFromat())) {
                    score += 6;
                }
                if (!alphaClient.get(0).getDataType().contains("metaData")) {
                    score -= 1;
                }
             }

        }

        List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> betaAlpha = filter(service,"Beta", "Alpha");
        List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> alphaBeta = filter(service,"Alpha", "Beta");

        if (betaAlpha.size() == 1 && alphaBeta.size() == 1) {
            if ("http".equals(betaAlpha.get(0).getType()) && "http".equals(alphaBeta.get(0).getType())) {
                if ("json".equalsIgnoreCase(alphaBeta.get(0).getDataFromat()) || "xml".equalsIgnoreCase(alphaBeta.get(0).getDataFromat())) {
                    score+=10;
                }
                else if ("plain".equalsIgnoreCase(alphaBeta.get(0).getDataFromat())) {
                    score+=6;
                }

                if (!betaAlpha.get(0).getDataType().contains("metaData")) {
                    score -= 1;
                }


            }
        }

        if (filter(service,"Alpha", "Gamma", "mq").size() == 1) {
            score +=5;
        }
        if (filter(service,"Gamma", "Alpha", "mq").size() == 1) {
            score +=5;
        }
        if (filter(service,"Gamma", "Delta", "mq").size() == 1) {
            score +=5;
        }
        if (filter(service,"Delta", "Gamma", "mq").size() == 1) {
            score +=5;
        }
        if (filter(service,"Omega", "Alpha", "http").size() == 1) {
            score +=5;
        }

        List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> omegaAlpha = filter(service,"Omega", "Alpha");
        if (omegaAlpha.size() == 2) {
            if ("http".equals(omegaAlpha.get(0).getType()) && "http".equals(omegaAlpha.get(1).getType())) {
                score+=10;

                if (!omegaAlpha.get(0).getDataType().contains("metaData")) {
                    score -= 1;
                }
            }
        }

        Set<ProviderConsumer> pcAllowedSet = new HashSet<>();
        pcAllowedSet.add(new ProviderConsumer("Alpha","Client"));
        pcAllowedSet.add(new ProviderConsumer("Alpha","Beta"));
        pcAllowedSet.add(new ProviderConsumer("Alpha","Gamma"));
        pcAllowedSet.add(new ProviderConsumer("Beta","Alpha"));
        pcAllowedSet.add(new ProviderConsumer("Gamma","Alpha"));
        pcAllowedSet.add(new ProviderConsumer("Gamma","Delta"));
        pcAllowedSet.add(new ProviderConsumer("Delta","Gamma"));
        pcAllowedSet.add(new ProviderConsumer("Omega","Alpha"));

        for (ru.hilariousstartups.analysttask1.task2.gen.Service.Integration s : service.getIntegration()) {
            if (!pcAllowedSet.contains(new ProviderConsumer(s.getProvider(), s.getConsumer()))) {
                score -= 3;
            }
        }

        return score;
    }

    private List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> filter(ru.hilariousstartups.analysttask1.task2.gen.Service service, String provider, String consumer) {
        return service.getIntegration().stream()
                .filter(i -> provider.equals(i.getProvider()) && consumer.equals(i.getConsumer()))
                .collect(Collectors.toList());
    }

    private List<ru.hilariousstartups.analysttask1.task2.gen.Service.Integration> filter(ru.hilariousstartups.analysttask1.task2.gen.Service service, String provider, String consumer, String type) {
        return service.getIntegration().stream()
                .filter(i -> provider.equals(i.getProvider()) && consumer.equals(i.getConsumer()) && type.equalsIgnoreCase(i.getType()))
                .collect(Collectors.toList());
    }
}
